package cz.it4i.parallel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.ImageJServerParadigm.Host;
import cz.it4i.parallel.persistence.PersistentParallelizationParadigmImpl;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;

public class TestParadigmPersistent extends TestParadigm implements
	PersistentParallelizationParadigm
{


	public static ParallelizationParadigm localImageJServer( String fiji, Context context ) {
		return new TestParadigmPersistent( new ImageJServerRunner( fiji ), context );
	}

	public static PersistentParallelizationParadigm runningImageJServer(
		Context context, HPCImageJServerRunnerWithUI runner,
		boolean stopImageJServerOnClose)
	{
		return new TestParadigmPersistent(new PNonClosingServerRunner(runner,
			stopImageJServerOnClose), context);
	}

	public TestParadigmPersistent(ServerRunner runner, Context context)
	{
		super(runner, initParadigm(runner, context));
	}
	
	

	private static class PNonClosingServerRunner extends TestServerRunner {

		final boolean started;

		public PNonClosingServerRunner(ServerRunner serverRunner, boolean started) {
			super(serverRunner);
			this.started = started;
		}

		@Override
		public void start() {
			if (!started) {
				super.start();
			}
		}

		@Override
		public void close() {
			// do nothing
		}
	}

	private static PersistentParallelizationParadigm initParadigm(
		ServerRunner runner, Context context)
	{
		runner.start();
		int nCores = runner.getNCores();
		List<Host> hosts = runner.getPorts().stream()
			.map(port -> new ImageJServerParadigm.Host("localhost:" + port, nCores))
				.collect( Collectors.toList() );
		return configureParadigm( context.service( ParallelService.class ), hosts );
	}

	private static PersistentParallelizationParadigm configureParadigm(
		ParallelService parallelService, List<Host> hosts)
	{
		parallelService.deleteProfiles();
		parallelService.addProfile(new ParallelizationParadigmProfile(
			ImageJServerParadigm.class, "lonelyBiologist01"));
		parallelService.selectProfile("lonelyBiologist01");

		ParallelizationParadigm paradigm = parallelService.getParadigm();
		((ImageJServerParadigm) paradigm).setHosts(hosts.subList(0, 1));
		paradigm.init();
		return PersistentParallelizationParadigmImpl.addPersistencyToParadigm(
			paradigm, hosts);
	}

	@Override
	public List<CompletableFutureID> getIDs(
		List<CompletableFuture<Map<String, Object>>> future)
	{
		checkClosed();
		return getPersistentParadigm().getIDs(future);
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> getByIDs(
		List<CompletableFutureID> ids)
	{
		checkClosed();
		return getPersistentParadigm().getByIDs(ids);
	}

	@Override
	public void purge(List<CompletableFutureID> ids) {
		checkClosed();
		getPersistentParadigm().purge(ids);
	}



	@Override
	public Collection<CompletableFuture<Map<String, Object>>> getAll() {
		checkClosed();
		return getPersistentParadigm().getAll();
	}

	private PersistentParallelizationParadigm getPersistentParadigm() {
		return (PersistentParallelizationParadigm) getParadigm();
	}

}
