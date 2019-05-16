package cz.it4i.parallel.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.HPCImageJServerRunner;
import cz.it4i.parallel.ImageJServerParadigm.Host;
import cz.it4i.parallel.persistence.PersistentParallelizationParadigmImpl;

public class TestParadigmPersistent 
{

	public static PersistentParallelizationParadigm addPersistency(
		ParallelizationParadigm paradigm,
		HPCImageJServerRunner runner)
	{
		int port = runner.getPorts().get(0);
		List<String> hosts = runner.getJob().getNodes();
		Integer cores = runner.getNCores();

		return PersistentParallelizationParadigmImpl.addPersistencyToParadigm(
			paradigm, hosts.stream().map(h -> h + ":" + port).map(n -> new Host(n,
				cores)).collect(Collectors.toList()));

	}



}
