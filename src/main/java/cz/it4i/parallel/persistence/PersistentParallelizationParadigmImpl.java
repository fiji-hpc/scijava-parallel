package cz.it4i.parallel.persistence;

import com.google.common.collect.Streams;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.Host;
import cz.it4i.parallel.MultipleHostParadigm;
import cz.it4i.parallel.RunningRemoteServer;
import cz.it4i.parallel.plugins.RequestBrokerServiceCallCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceGetResultCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceInitCommand;
import cz.it4i.parallel.plugins.RequestBrokerServicePurgeCommand;
import lombok.AllArgsConstructor;
import lombok.Data;



public class PersistentParallelizationParadigmImpl implements
	PersistentParallelizationParadigm
{

	public static final String INPUTS = "inputs";
	public static final String MODULE_ID = "moduleId";
	public static final String REQUEST_IDS = "requestIDs";
	public static final String RESULTS = "results";

	enum CompletableFutureIDCases implements CompletableFutureID {
			UNKNOWN;
	}

	private final ParallelizationParadigm paradigm;


	private final Map<CompletableFuture<Map<String, Object>>, Serializable> futures2id =
		new HashMap<>();
	private final Map<Serializable, CompletableFuture<Map<String, Object>>> id2futures =
		new HashMap<>();


	private List<Host> hosts;

	private PInnerCallStrategy callStrategy;

	public static PersistentParallelizationParadigm addPersistencyToParadigm(
		ParallelizationParadigm paradigm, RunningRemoteServer runningServer)
	{
		return addPersistencyToParadigm(paradigm, runningServer, paradigm.getClass()
			.getCanonicalName());
	}

	public static PersistentParallelizationParadigm addPersistencyToParadigm(
		ParallelizationParadigm paradigm, RunningRemoteServer runningServer,
		String paradigmClassName)
	{

		List<Host> hosts = Streams.zip(Streams.zip(runningServer.getRemoteHosts()
			.stream(), runningServer.getRemotePorts().stream(), (host, port) -> host +
				":" + port), runningServer.getNCores().stream(), Host::new).collect(
					Collectors.toList());
		PInnerCallStrategy callStrategy;
		if (!(paradigm instanceof MultipleHostParadigm) && hosts.size() > 1) {
			throw new IllegalArgumentException(
				"Only MultipleHostsParadigm with multiple hosts is allowed.");
		}
		else if (hosts.size() > 1) {
			MultipleHostParadigm multipleHostParadigm =
				(MultipleHostParadigm) paradigm;
			callStrategy = new PInnerCallStrategyMoreHost(multipleHostParadigm,
				multipleHostParadigm.getHosts().get(0));
		} else {
			callStrategy = new PInnerCallStrategyOneHost(paradigm);
		}
		PersistentParallelizationParadigmImpl result =
			new PersistentParallelizationParadigmImpl(paradigm, callStrategy);
		result.setHosts(hosts);
		result.initRemoteParallelizationParadigm(paradigmClassName);
		return result;
	}

	private PersistentParallelizationParadigmImpl(
		ParallelizationParadigm paradigmParam, PInnerCallStrategy callStrategy)
	{
		paradigm = paradigmParam;
		this.callStrategy = callStrategy;
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
		inputForExecution.put(MODULE_ID, commandTypeName);
		inputForExecution.put(INPUTS, inputs);

		@SuppressWarnings("unchecked")
		List<Serializable> result = (List<Serializable>) callStrategy.call(
			RequestBrokerServiceCallCommand.class.getCanonicalName(),
			inputForExecution).get(REQUEST_IDS);

		return result.stream().map(this::getFuture4FutureID).collect(Collectors
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
		inputForExecution.put(REQUEST_IDS, new LinkedList<>(ids));
		callStrategy.call(RequestBrokerServicePurgeCommand.class.getCanonicalName(),
			inputForExecution);
	}

	@Override
	public Collection<CompletableFuture<Map<String, Object>>> getAll() {

		throw new UnsupportedOperationException();
//			return (Collection<CompletableFuture<Map<String, Object>>>) paradigm
//				.runAllAsync(RequestBrokerServiceGetAllCommand.class.getCanonicalName(),
//					Collections.emptyList()).get(0).get().get(REQUEST_IDS);

	}

	private void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}

	private void initRemoteParallelizationParadigm(String paradigmClassName) {
		Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put("names", hosts.stream().map(Host::getName).collect(
			Collectors.toList()));
		inputForExecution.put("ncores", hosts.stream().map(Host::getNCores).collect(
			Collectors.toList()));
		inputForExecution.put("paradigmClassName", paradigmClassName);
		callStrategy.call(RequestBrokerServiceInitCommand.class.getCanonicalName(),
			inputForExecution);

	}

	@SuppressWarnings("unchecked")
	private synchronized CompletableFuture<Map<String, Object>>
		getFuture4FutureID(Serializable requestID)
	{
		if (requestID instanceof PComletableFutureID) {
			requestID = ((PComletableFutureID) requestID).getInnerId();

		}
		if (id2futures.containsKey(requestID)) {
			return id2futures.get(requestID);
		}

		if (requestID == CompletableFutureIDCases.UNKNOWN) {
			return CompletableFuture.supplyAsync(() -> {
				throw new IllegalStateException();
			});
		}
		final Map<String, Object> inputForExecution = new HashMap<>();
		inputForExecution.put(REQUEST_IDS, new LinkedList<>(Collections.singleton(
			requestID)));
		CompletableFuture<Map<String, Object>> resultFuture = callStrategy
			.callAsync(RequestBrokerServiceGetResultCommand.class.getCanonicalName(),
				inputForExecution).thenApply(
					result -> ((List<Map<String, Object>>) result.get(RESULTS)).get(0));

		id2futures.put(requestID, resultFuture);
		futures2id.put(resultFuture, requestID);
		return resultFuture;

	}

	private synchronized CompletableFutureID getFutureID4Future(
		CompletableFuture<Map<String, Object>> future)
	{
		if (futures2id.containsKey(future)) {
			return PComletableFutureID.getFutureID(futures2id.get(future));
		}
		return CompletableFutureIDCases.UNKNOWN;
	}

	private void removeFutureID(Object futureID) {
		if (futureID == CompletableFutureIDCases.UNKNOWN) {
			return;
		}
		if (futureID instanceof CompletableFutureID) {
			CompletableFutureID pId = (CompletableFutureID) futureID;
			if (id2futures.containsKey(pId)) {
				CompletableFuture<Map<String, Object>> future = id2futures.get(pId);
				id2futures.remove(pId);
				futures2id.remove(future);
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported type " + futureID);
		}
	}

	@Data
	@AllArgsConstructor
	private static class PComletableFutureID implements CompletableFutureID {

		final Serializable innerId;

		static CompletableFutureID getFutureID(Serializable id) {
			if (id instanceof CompletableFutureID) {
				return (CompletableFutureID) id;
			}
			return new PComletableFutureID(id);
		}
	}

	private interface PInnerCallStrategy {

		Map<String, Object> call(String commandName, Map<String, Object> inputs);

		CompletableFuture<Map<String, Object>> callAsync(String commandName,
			Map<String, Object> inputs);
	}

	private static class PInnerCallStrategyOneHost implements PInnerCallStrategy {

		private ParallelizationParadigm paradigm;

		public PInnerCallStrategyOneHost(ParallelizationParadigm paradigm) {
			this.paradigm = paradigm;
		}

		@Override
		public Map<String, Object> call(String commandName,
			Map<String, Object> inputs)
		{
			return paradigm.runAll(commandName, Collections.singletonList(inputs))
				.get(0);
		}

		@Override
		public CompletableFuture<Map<String, Object>> callAsync(String commandName,
			Map<String, Object> inputs)
		{
			return paradigm.runAllAsync(commandName, Collections.singletonList(
				inputs)).get(0);
		}

	}

	private static class PInnerCallStrategyMoreHost implements
		PInnerCallStrategy
	{

		private MultipleHostParadigm paradigm;
		private String hostName;

		public PInnerCallStrategyMoreHost(MultipleHostParadigm paradigm,
			String hostName)
		{
			this.paradigm = paradigm;
			this.hostName = hostName;
		}

		@Override
		public Map<String, Object> call(String commandName,
			Map<String, Object> inputs)
		{
			return paradigm.runOnHosts(commandName, inputs, Collections.singletonList(
				hostName)).get(0);
		}

		@Override
		public CompletableFuture<Map<String, Object>> callAsync(String commandName,
			Map<String, Object> inputs)
		{
			return CompletableFuture.supplyAsync(() -> call(commandName, inputs));
		}

	}
}
