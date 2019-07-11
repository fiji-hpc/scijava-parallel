package cz.it4i.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractMultipleHostParadigm extends AbstractBaseParadigm
	implements MultipleHostParadigm
{

	private final List<String> hosts = new LinkedList<>();

	private Map<String, ParallelWorker> hostName2Worker = new HashMap<>();

	@Override
	public void setHosts(Collection<Host> hosts) {
		this.hosts.clear();
		this.hosts.addAll(hosts.stream().map(Host::getName).collect(Collectors
			.toList()));
		int ncores = hosts.iterator().next().getNCores();
		if (!hosts.stream().allMatch(host -> host.getNCores() == ncores)) {
			throw new UnsupportedOperationException(
				"Only hosts with same number of cores are supported");
		}
	}

	@Override
	public List<String> getHosts() {
		return hosts;
	}

	@Override
	public List<Map<String, Object>> runOnHosts(String commandName,
		Map<String, Object> parameters, List<String> usedHosts)
	{
		return usedHosts.parallelStream().map(name -> hostName2Worker.get(name))
			.map(worker -> worker.executeCommand(commandName, Collections
				.singletonList(
				parameters))).map(result -> result.get(0)).collect(Collectors.toList());

	}

	@Override
	protected void initWorkerPool() {
		hosts.forEach(this::createAndIndexWorker);
	}

	protected abstract ParallelWorker createWorker(String host);

	private void createAndIndexWorker(String host) {
		ParallelWorker worker = createWorker(host);
		hostName2Worker.put(host, worker);
		workerPool.addWorker(worker);
	}
}
