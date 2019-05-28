
package cz.it4i.parallel.imagej.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.parallel.AbstractBaseParadigm;
import cz.it4i.parallel.Host;
import cz.it4i.parallel.MultipleHostParadigm;
import cz.it4i.parallel.ParallelWorker;

@Plugin(type = ParallelizationParadigm.class)
public class ImageJServerParadigm extends AbstractBaseParadigm implements
	MultipleHostParadigm
{

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.imagej.server.ImageJServerParadigm.class);
	
	@Parameter
	private Context context;

	private int port;

	@Parameter
	private PluginService pluginService;

	private final Collection<String> hosts = new LinkedList<>();

	private ParameterTypeProvider typeProvider;

	private Map<Class<?>, ParallelizationParadigmConverter<?>> mappers;

	// -- ImageJServerParadigm methods --

	public void setPort(final int port) {
		this.port = port;
	}

	@Override
	public void setHosts(final Collection<Host> hosts) {
		this.hosts.clear();
		this.hosts.addAll(hosts.stream().map(Host::getName).collect(
			Collectors.toList()));
		int ncores = hosts.iterator().next().getNCores();
		if (!hosts.stream().allMatch(host -> host.getNCores() == ncores)) {
			throw new UnsupportedOperationException(
				"Only hosts with same number of cores are supported");
		}
	}

	// -- SimpleOstravaParadigm methods --

	@Override
	protected void initWorkerPool() {
		typeProvider = new DefaultParameterTypeProvider(port, hosts.iterator()
			.next());
		hosts.forEach(host -> workerPool.addWorker(createWorker(host)));
	}

	private ParallelWorker createWorker(String host) {
		if (host.contains(":")) {
			final String[] tokensOfHost = host.split(":");
			port = Integer.parseInt(tokensOfHost[1]);
			host = tokensOfHost[0];
		}
		
		return new ImageJServerWorker(host, port, context, typeProvider,
			getMappers());
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
		pluginService.createInstancesOfType(ParallelizationParadigmConverter.class)
			.stream().filter(this::isParadigmSupportedBy).forEach(m -> mappers.put(m
				.getOutputType(), m));

	}

	private boolean isParadigmSupportedBy(ParallelizationParadigmConverter<?> m) {
		for (Class<? extends ParallelizationParadigm> clazz : m
			.getSupportedParadigms())
		{
			if (clazz.isAssignableFrom(this.getClass())) {
				return true;
			}
		}
		return false;
	}
}
