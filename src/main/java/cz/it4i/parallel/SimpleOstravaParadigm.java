
package cz.it4i.parallel;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.thread.ThreadService;

import cz.it4i.common.Collections;
import cz.it4i.parallel.persistence.RequestBrokerServiceParameterProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SimpleOstravaParadigm implements ParallelizationParadigm {

	protected WorkerPool workerPool;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private Context context;

	private Map<Class<?>, ParallelizationParadigmConverter<?>> mappers;

	private ExecutorService executorService;

	private RequestBrokerServiceParameterProvider requestBrokerServiceParameterProvider;

	// -- SimpleOstravaParadigm methods --

	protected abstract void initWorkerPool();

	// -- ParallelizationParadigm methods --

	@Override
	public void init() {
		workerPool = new WorkerPool();
		initWorkerPool();
		executorService = Executors.newFixedThreadPool(workerPool.size(),
			threadService);
		requestBrokerServiceParameterProvider =
			new RequestBrokerServiceParameterProvider(getTypeProvider(),
				getMappers(), constructDefaultWorker());
		context.inject(requestBrokerServiceParameterProvider);
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> runAllAsync(
		String command, List<Map<String, Object>> listOfparameters)
	{
		if (listOfparameters.isEmpty()) {
			return java.util.Collections.emptyList();
		}
		log.debug("runAllAsync - params.size = {}", listOfparameters.size());
		List<List<Map<String, Object>>> chunkedParameters = chunkParameters(
			listOfparameters);

		return chunkedParameters.parallelStream().map(
			inputs -> new AsynchronousExecution(command, inputs)).map(
				ae -> repackCompletable(ae.result, ae.size)).flatMap(List::stream)
			.collect(Collectors.toList());
	}

	protected List<List<Map<String, Object>>> chunkParameters(
		List<Map<String, Object>> listOfparameters) {
		return Lists.partition(listOfparameters, 24);
	}

	@Override
	public void close() {
		workerPool.close();
		executorService.shutdown();
		threadService.dispose();
	}

	protected abstract ParameterTypeProvider getTypeProvider();

	protected ParameterProcessor constructParameterProcessor(RemoteDataHandler pw,
		String command)
	{

		ParameterProcessor result = requestBrokerServiceParameterProvider
			.constructProvider(command, pw);
		if (result == null) {
			result = new DefaultParameterProcessor(getTypeProvider(),
			command, pw, getMappers());
		}
		return result;
	}

	private RemoteDataHandler constructDefaultWorker() {
		if (workerPool.size() != 1) {
			return null;
		}
		ParallelWorker pw = Routines.supplyWithExceptionHandling(() -> workerPool
			.takeFreeWorker());
		workerPool.addWorker(pw);
		return pw;
	}

	private synchronized Map<Class<?>, ParallelizationParadigmConverter<?>>
		getMappers()
	{
		if (mappers == null) {
			mappers = new HashMap<>();
			initMappers();
		}
		return mappers;
	}

	private void initMappers() {
		pluginService.createInstancesOfType(
			ParallelizationParadigmConverter.class).stream().filter(
				this::isParadigmSupportedBy).forEach(m -> mappers.put(m
					.getOutputType(), m));

	}

	private boolean isParadigmSupportedBy(
		ParallelizationParadigmConverter<?> m)
	{
		for (Class<? extends ParallelizationParadigm> clazz : m
			.getSupportedParadigms())
		{
			if (clazz.isAssignableFrom(this.getClass())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static <T> List<CompletableFuture<T>> repackCompletable(
		CompletableFuture<List<T>> input, int size)
	{
		CompletableFuture<Object[]> array = input.thenApply(List<T>::toArray);
		List<CompletableFuture<T>> result = new LinkedList<>();
		for(int i = 0; i < size; i++) {
			final int index = i;
			result.add(array.thenApply(list -> (T) list[index]));
		}
		return result;
	}
	
	private class AsynchronousExecution {
		
		public final int size;

		public final CompletableFuture<List<Map<String, Object>>> result;

		public AsynchronousExecution(String command,
			List<Map<String, Object>> inputs)
		{
			result = supplyAsync(() -> executeForInputs(command, inputs));
			size = inputs.size();
		}

		private List<ParameterProcessor> constructParameterProcessors(
			RemoteDataHandler pw,
			String command, List<Map<String, Object>> inputs)
		{
			return IntStream.range(0, inputs.size()).mapToObj(
				__ -> constructParameterProcessor(pw, command)).collect(Collectors // NOSONAR
					.toList());
		}

		private List<Map<String, Object>> executeForInputs(String command,
			List<Map<String, Object>> inputs)
		{
			ParallelWorker pw;
			try {
				pw = workerPool.takeFreeWorker();
			}
			catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(exc); // NOSONAR
			}
			List<ParameterProcessor> processors;
			try (AutoCloseable parameterProcessor = Collections.closeAll(processors = // NOSONAR
				constructParameterProcessors(pw, command, inputs)))
			{

				inputs = processValues(inputs, processors,
					ParameterProcessor::processInputs);
				List<Map<String, Object>> outputs = pw.executeCommand(command, inputs);
				return processValues(outputs, processors,
					ParameterProcessor::processOutputs);
			}
			catch (RuntimeException e) {
				// this should be rethrown
				throw e;
			}
			catch (Exception e) {
				// this should not happen!
				throw new RuntimeException(e);// NOSONAR
			}
			finally {
				workerPool.addWorker(pw);
			}
		}

		private List<Map<String, Object>> processValues(
			List<Map<String, Object>> values, List<ParameterProcessor> processors,
			BiFunction<ParameterProcessor, Map<String, Object>, Map<String, Object>> func)
		{
			return Streams.zip(values.stream(), processors.stream(), (i, p) -> func
				.apply(p, i)).collect(Collectors.toList());
		}
	}
}
