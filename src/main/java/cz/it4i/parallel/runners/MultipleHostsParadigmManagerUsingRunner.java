package cz.it4i.parallel.runners;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.stream.Collectors;

import cz.it4i.parallel.AbstractBaseParadigm;
import cz.it4i.parallel.Host;
import cz.it4i.parallel.MultipleHostParadigm;


public abstract class MultipleHostsParadigmManagerUsingRunner<T extends AbstractBaseParadigm & MultipleHostParadigm>
	extends ParadigmManagerUsingRunner<T>
{

	@Override
	protected void initParadigm(ParadigmProfileUsingRunner typedProfile,
		T paradigm) {
		ServerRunner runner = typedProfile.getAssociatedRunner();
		List<Host> hosts = Streams.zip(runner.getPorts().stream(), runner
			.getNCores().stream(), (Integer port, Integer nCores) -> new Host(
				"localhost:" + port, nCores)).collect(Collectors.toList());
		paradigm.setHosts(hosts);	
	}

}
