package cz.it4i.parallel.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import cz.it4i.parallel.Host;
import cz.it4i.parallel.MultipleHostParadigm;
import cz.it4i.parallel.SciJavaParallelRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Plugin(type = Service.class)
public class DefaultRequestBrokerService extends AbstractService implements
	RequestBrokerService
{

	public enum State {
			DONE, CANCELLED, COMPLETED_EXCEPTIONALLY, RUNNING

	}
	
	private static final Map<Predicate<CompletableFuture<?>>, State> STATE_MAP;

	static {
		Map<Predicate<CompletableFuture<?>>, State> initMap = new LinkedHashMap<>();
		initMap.put(f -> f.isDone(), State.DONE);
		initMap.put(f -> f.isCancelled(), State.CANCELLED);
		initMap.put(f -> f.isCompletedExceptionally(),
			State.COMPLETED_EXCEPTIONALLY);
		STATE_MAP = Collections.unmodifiableMap(initMap);

	}

	@Parameter
	private ParallelService parallelService;

	private boolean paradigmInitialized;

	private Map<Object, CompletableFuture<Map<String, Object>>> holdedRequests =
		Collections.synchronizedMap(new HashMap<>());

	@Override
	public synchronized void initParallelizationParadigm(String paradigmClassName,
		List<String> hostNames,
		List<Integer> ncores)
	{
		try {
			if (!paradigmInitialized) {
				parallelService.deleteProfiles();
				@SuppressWarnings("unchecked")
				Class<? extends ParallelizationParadigm> clazz = (Class<? extends ParallelizationParadigm>) Class.forName(
					paradigmClassName);
				parallelService.addProfile(new ParallelizationParadigmProfile(clazz,
					clazz.getName()));
				parallelService.selectProfile(clazz.getName());
				ParallelizationParadigm paradigm = parallelService.getParadigm();
				((MultipleHostParadigm) paradigm).setHosts(Host
					.constructListFromNamesAndCores(hostNames, ncores));
				paradigm.init();
				paradigmInitialized = true;
			}
			else {
				log.info("Parallelization paradigm already initialized");
			}
		}
		catch (ClassNotFoundException exc) {
			throw new SciJavaParallelRuntimeException(exc);
		}
	}

	@Override
	public List<Object> call(String commandName,
		List<Map<String, Object>> parameters)
	{

		List<CompletableFuture<Map<String, Object>>> futures = doCall(commandName,
			parameters);
		assert futures != null;
		List<Object> result = new LinkedList<>();
		// @formatter:off
		futures.stream()
					.forEach(future -> holdedRequests.put(addToList(result, generateID(future)),future));
		// @formatter:on
		return result;
	}

	@Override
	public List<Map<String, Object>> getResult(List<Object> requestID) {
		return requestID.stream().map(id -> getResult(holdedRequests.get(id)))
			.collect(
			Collectors.toList());
	}

	@Override
	public List<State> getState(List<Object> requestID) {
		return requestID.stream().map(id -> getState(holdedRequests.get(id)))
			.collect(Collectors.toList());

	}

	@Override
	public void purge(List<Object> requestID) {
		requestID.stream().forEach(id -> holdedRequests.remove(id));
	}

	@Override
	public Collection<Object> getAllRequests() {
		return holdedRequests.keySet();
	}

	private Object addToList(List<Object> result, Object generateID) {
		result.add(generateID);
		return generateID;
	}

	private List<CompletableFuture<Map<String, Object>>> doCall(
		String commandName,
		List<Map<String, Object>> parameters)
	{
		return parallelService.getParadigm().runAllAsync(commandName, parameters);
	}



	private State getState(
		CompletableFuture<Map<String, Object>> completableFuture)
	{
		for (Entry<Predicate<CompletableFuture<?>>, State> entry : STATE_MAP
			.entrySet())
		{
			if (entry.getKey().test(completableFuture)) {
				return entry.getValue();
			}
		}
		return State.RUNNING;
	}

	private Map<String, Object> getResult(
		CompletableFuture<Map<String, Object>> completableFuture)
	{
		try {
			return completableFuture.get();
		}
		catch (ExecutionException exc) {
			log.warn(exc.getMessage(), exc);
			return null;
		}
		catch (InterruptedException exc) {
			return null;
		}
	}

	private Object generateID(
		@SuppressWarnings("unused") CompletableFuture<Map<String, Object>> future)
	{
		return UUID.randomUUID().toString();
	}

}
