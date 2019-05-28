package cz.it4i.parallel.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.scijava.parallel.PersistentParallelizationParadigm;

import cz.it4i.parallel.runners.ServerRunner;

public class TestPersistentParadigm extends TestParadigm implements
	PersistentParallelizationParadigm
{

	private PersistentParallelizationParadigm paradigm;

	public TestPersistentParadigm(PersistentParallelizationParadigm paradigm,
		ServerRunner runner)
	{
		super(paradigm, runner);
		this.paradigm = paradigm;
	}

	@Override
	public List<CompletableFutureID> getIDs(
		List<CompletableFuture<Map<String, Object>>> future)
	{
		return paradigm.getIDs(future);
	}

	@Override
	public List<CompletableFuture<Map<String, Object>>> getByIDs(
		List<CompletableFutureID> ids)
	{
		return paradigm.getByIDs(ids);
	}

	@Override
	public void purge(List<CompletableFutureID> ids) {
		paradigm.purge(ids);
	}

	@Override
	public Collection<CompletableFuture<Map<String, Object>>> getAll() {
		return paradigm.getAll();
	}

}
