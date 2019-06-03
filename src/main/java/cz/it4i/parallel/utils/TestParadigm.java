package cz.it4i.parallel.utils;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;

import cz.it4i.parallel.Host;
import cz.it4i.parallel.imagej.server.ImageJServerParadigm;
import cz.it4i.parallel.runners.ImageJServerRunner;
import cz.it4i.parallel.runners.ServerRunner;

public class TestParadigm implements ParallelizationParadigm
{

	private final ServerRunner runner;
	private final ParallelizationParadigm paradigm;
	private boolean closed = false;

	protected TestParadigm(ParallelizationParadigm paradigm,
		ServerRunner runner)
	{
		this.paradigm = paradigm;
		this.runner = runner;
	}

	public TestParadigm(ServerRunner runner, Context context)
	{
		this.paradigm = initParadigm( runner, context );
		this.runner = runner;
	}

	public TestParadigm(ServerRunner runner, ParallelizationParadigm paradigm)
	{
		super();
		this.runner = runner;
		this.paradigm = paradigm;
	}

	public static ParallelizationParadigm localImageJServer( String fiji, Context context ) {
		return new TestParadigm(new ImageJServerRunner(fiji, true), context);
	}


	private static ParallelizationParadigm initParadigm(ServerRunner runner,
		Context context)
	{
		runner.start();
		List<Host> hosts = Streams.zip(runner.getPorts().stream(), runner
			.getNCores().stream(), (port, nCores) -> new Host(
				"localhost:" + port, nCores))
				.collect( Collectors.toList() );
		return configureParadigm( context.service( ParallelService.class ), hosts );
	}

	private static ParallelizationParadigm configureParadigm(
		ParallelService parallelService, List<Host> hosts)
	{
		parallelService.deleteProfiles();
		parallelService.addProfile(new ParallelizationParadigmProfile(
				ImageJServerParadigm.class, "lonelyBiologist01"));
		parallelService.selectProfile("lonelyBiologist01");

		ParallelizationParadigm paradigm = parallelService.getParadigm();
		((ImageJServerParadigm) paradigm).setHosts(hosts);
		paradigm.init();
		return paradigm;
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
	public void close()
	{
		closed = true;
		paradigm.close();
		runner.close();
	}

	public ParallelizationParadigm getParadigm() {
		return paradigm;
	}

	void checkClosed()
	{
		if(closed)
			throw new RuntimeException( "ParallelizationParadigm is used after it has been closed." );
	}

}
