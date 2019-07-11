package cz.it4i.parallel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public abstract class AbstractMultipleHostParadigm extends AbstractBaseParadigm
	implements MultipleHostParadigm
{

	private final Collection<String> hosts = new LinkedList<>();

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
	protected void initWorkerPool() {
		hosts.forEach(host -> workerPool.addWorker(createWorker(host)));
	}

	protected abstract ParallelWorker createWorker(String host);
}
