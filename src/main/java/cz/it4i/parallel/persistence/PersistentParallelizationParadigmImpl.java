package cz.it4i.parallel.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.ImageJServerParadigm.Host;
import cz.it4i.parallel.plugins.RequestBrokerServiceCallCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceGetResultCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceInitCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceGetAllCommand;
import lombok.AllArgsConstructor;



public class PersistentParallelizationParadigmImpl implements
	PersistentParallelizationParadigm
{

	private static final String REQUEST_IDS = "requestIDs";

	enum CompletableFutureIDCases implements CompletableFutureID {
			UNKNOWN;
	}

	private final ParallelizationParadigm paradigm;


	private final Map<CompletableFuture<Map<String, Object>>, Object> futures2id =
		new HashMap<>();
	private final Map<Object, CompletableFuture<Map<String, Object>>> id2futures =
		new HashMap<>();
	private final Map<CompletableFuture<Map<String, Object>>, CompletableFutureID> futures2futureID =
		new WeakHashMap<>();

	private List<Host> hosts;

	public PersistentParallelizationParadigmImpl() {
		this(null);
	}

	public static PersistentParallelizationParadigm addPersistencyToParadigm(
		ParallelizationParadigm paradigm, List<Host> hosts)
	{
		PersistentParallelizationParadigmImpl result =
			new PersistentParallelizationParadigmImpl(paradigm);
		result.setHosts(hosts);
		result.initRemoteParallelizationParadigm();
		return result;
	}

	@Override
	public void init() {
	}

	@Override
	public void close() {
		paradigm.close();
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> runAllAsync(
		String commandTypeName, List<Map<String, Object>> inputs)
	{
		if (hosts == null) {
			throw new IllegalStateException();
		}
		Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put("moduleId", commandTypeName);
		inputForExecution.put("inputs", inputs);

		@SuppressWarnings("unchecked")
		List<Object> result = (List<Object>) paradigm.runAll(
			RequestBrokerServiceCallCommand.class.getCanonicalName(), Collections
				.singletonList(inputForExecution)).get(0).get(REQUEST_IDS);

		return result.stream().map(this::getFuture4ID).collect(Collectors
			.toList());
	}


	@Override
	public List<CompletableFutureID> getIDs(
		List<CompletableFuture<Map<String, Object>>> futures)
	{
		return futures.stream().map(this::getFutureID4Future).collect(Collectors
			.toList());
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> getByIDs(
		List<CompletableFutureID> ids)
	{
		return ids.stream().map(this::getFuture4FutureID).collect(Collectors
			.toList());
	}

	@Override
	public void purge(List<CompletableFutureID> ids) {
		final Map<String, Object> inputForExecution = new HashMap<>();
		synchronized (this) {
			ids.stream().forEach(this::removeFutureID);
		}
		inputForExecution.put("requestIDs", new LinkedList<>(ids));
		try {
			paradigm.runAllAsync(RequestBrokerServiceGetAllCommand.class
				.getCanonicalName(), Collections.singletonList(inputForExecution)).get(
					0).get();

		}
		catch (InterruptedException | ExecutionException exc) {
			throw new RuntimeException(exc);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<CompletableFuture<Map<String, Object>>> getAll() {
		try {
			return (Collection<CompletableFuture<Map<String, Object>>>) paradigm
				.runAllAsync(RequestBrokerServiceGetAllCommand.class.getCanonicalName(),
					Collections.emptyList()).get(0).get().get("requestIDs");
		}
		catch (InterruptedException | ExecutionException exc) {
			throw new RuntimeException(exc);
		}
	}

	private PersistentParallelizationParadigmImpl(
		ParallelizationParadigm paradigmParam)
	{
		paradigm = paradigmParam;
	}

	private void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

	private void initRemoteParallelizationParadigm() {
		Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put("names", hosts.stream().map(Host::getName).collect(
			Collectors.toList()));
		inputForExecution.put("ncores", hosts.stream().map(Host::getNCores)
			.collect(Collectors.toList()));
		paradigm.runAll(
				RequestBrokerServiceInitCommand.class.getCanonicalName(),
			Collections.singletonList(inputForExecution));
	
	}

	@SuppressWarnings("unchecked")
	private synchronized CompletableFuture<Map<String, Object>> getFuture4ID(
		Object id)
	{
		if (id2futures.containsKey(id)) {
			return id2futures.get(id);
		}
		final Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put("requestIDs", new LinkedList<>(Collections.singleton(
			id)));
		CompletableFuture<Map<String, Object>> resultFuture = paradigm.runAllAsync(
				RequestBrokerServiceGetResultCommand.class.getCanonicalName(),
			Collections.singletonList(inputForExecution)).get(0).thenApply(
				result -> ((List<Map<String, Object>>) result.get("results")).get(0));

		id2futures.put(id, resultFuture);
		futures2id.put(resultFuture, id);
		return resultFuture;
	}

	private synchronized CompletableFutureID getFutureID4Future(
		CompletableFuture<Map<String, Object>> future)
	{
		if (futures2futureID.containsKey(future)) {
			return futures2futureID.get(future);
		}
		if (futures2id.containsKey(future)) {
			Object id = futures2id.get(future);
			CompletableFutureID result = new PCompletableFutureID(id);
			futures2futureID.put(future, result);
			return result;
		}
		return CompletableFutureIDCases.UNKNOWN;
	}

	private synchronized CompletableFuture<Map<String, Object>>
		getFuture4FutureID(CompletableFutureID id)
	{
		if (id == CompletableFutureIDCases.UNKNOWN) {
			return CompletableFuture.supplyAsync(() -> {
				throw new IllegalStateException();
			});
		}
		if (id instanceof PCompletableFutureID) {
			PCompletableFutureID pId = (PCompletableFutureID) id;
			return getFuture4ID(pId.id);
		}
		throw new IllegalArgumentException("Unsupported type " + id);
	}

	private void removeFutureID(CompletableFutureID futureID) {
		if (futureID == CompletableFutureIDCases.UNKNOWN) {
			return;
		}
		if (futureID instanceof PCompletableFutureID) {
			PCompletableFutureID pId = (PCompletableFutureID) futureID;
			if (id2futures.containsKey(pId.id)) {
				CompletableFuture<Map<String, Object>> future = id2futures.get(pId.id);
				id2futures.remove(pId.id);
				futures2id.remove(future);
				if (futures2futureID.containsKey(future)) {
					futures2futureID.remove(future);
				}
			}
		}
		throw new IllegalArgumentException("Unsupported type " + futureID);
	}

	@AllArgsConstructor
	private class PCompletableFutureID implements CompletableFutureID {

		final Object id;
	}


}
