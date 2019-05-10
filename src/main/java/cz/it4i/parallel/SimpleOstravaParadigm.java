
package cz.it4i.parallel;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SimpleOstravaParadigm implements ParallelizationParadigm {

	protected WorkerPool workerPool;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private Context context;

	private ExecutorService executorService;

	// -- SimpleOstravaParadigm methods --

	protected abstract void initWorkerPool();

	// -- ParallelizationParadigm methods --

	@Override
	public void init() {
		workerPool = new WorkerPool();
		initWorkerPool();
		executorService = Executors.newFixedThreadPool(workerPool.size(),
			threadService);

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
			try
			{
				return pw.executeCommand(command, inputs);
			}
			catch (RuntimeException e) {
				// this should be rethrown
				throw e;
			}
			catch (Exception e) {
				// this should not happen!
				throw new SciJavaParallelRuntimeException(e);
			}
			finally {
				workerPool.addWorker(pw);
			}
		}


	}
}
