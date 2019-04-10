package cz.it4i.parallel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.ImageJServerParadigm.Host;
import cz.it4i.parallel.persistence.PersistentParallelizationParadigmImpl;

public class TestParadigmPersistent implements
	PersistentParallelizationParadigm
{

	private final ServerRunner runner;
	private final PersistentParallelizationParadigm paradigm;
	private boolean closed = false;

	public TestParadigmPersistent(ServerRunner runner, Context context)
	{
		this.paradigm = initParadigm( runner, context );
		this.runner = runner;
	}

	public static ParallelizationParadigm localImageJServer( String fiji, Context context ) {
		return new TestParadigmPersistent( new ImageJServerRunner( fiji ), context );
	}

	public static ParallelizationParadigm localImageJServer(Context context) {
		return new TestParadigmPersistent(new EmbeddedImageJServerRunner(), context);
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
		PersistentParallelizationParadigm result =
			PersistentParallelizationParadigmImpl.addPersistencyToParadigm(paradigm,
				hosts);

		return result;
	}

	@Override
	public void init()
	{
		checkClosed();
		paradigm.init();
	}

	@Override
	public List< Map< String, Object > > runAll( Class< ? extends Command > commandClazz, List< Map< String, Object > > parameters )
	{
		checkClosed();
		return paradigm.runAll( commandClazz, parameters );
	}

	@Override
	public List< CompletableFuture< Map< String, Object > > > runAllAsync( Class< ? extends Command > commandClazz, List< Map< String, Object > > parameters )
	{
		checkClosed();
		return paradigm.runAllAsync( commandClazz, parameters );
	}

	@Override
	public List< Map< String, Object > > runAll( String s, List< Map< String, Object > > list )
	{
		checkClosed();
		return paradigm.runAll( s, list );
	}

	@Override
	public List< CompletableFuture< Map< String, Object > > > runAllAsync( String s, List< Map< String, Object > > list )
	{
		checkClosed();
		return paradigm.runAllAsync( s, list );
	}

	@Override
	public List<CompletableFutureID> getIDs(
		List<CompletableFuture<Map<String, Object>>> future)
	{
		checkClosed();
		return paradigm.getIDs(future);
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> getByIDs(
		List<CompletableFutureID> ids)
	{
		checkClosed();
		return paradigm.getByIDs(ids);
	}

	@Override
	public void purge(List<CompletableFutureID> ids) {
		checkClosed();
		paradigm.purge(ids);
	}

	@Override
	public Collection<CompletableFuture<Map<String, Object>>> getAll() {
		checkClosed();
		return paradigm.getAll();
	}

	@Override
	public void close()
	{
		closed = true;
		paradigm.close();
		runner.close();
	}

	private void checkClosed()
	{
		if(closed)
			throw new RuntimeException( "ParallelizationParadigm is used after it has been closed." );
	}

}
