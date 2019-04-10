
package test;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.plugins.commands.imglib.RotateImageXY;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm.CompletableFutureID;
import org.scijava.ui.UIService;

import cz.it4i.parallel.AbstractImageJServerRunner;
import cz.it4i.parallel.ServerRunner;
import cz.it4i.parallel.TestParadigmPersistent;
import cz.it4i.parallel.TestServerRunner;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;

public class RotateSingleDatasetWithPersistenceOnHPC
{

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

	public static void main(String[] args) throws InterruptedException,
		ExecutionException
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		Context context = ij.getContext();
		DatasetIOService ioService = context.service(DatasetIOService.class);
		UIService uiService = context.service(UIService.class);
		try (AbstractImageJServerRunner runner = HPCImageJServerRunnerWithUI.gui(
			context))
		{
			List<CompletableFutureID> futureIDs;
			try (PersistentParallelizationParadigm paradigm =
				new TestParadigmPersistent(
					new PNonClosingServerRunner(runner, false), context))
			{
				List<Map<String, Object>> parametersList = initParameters(ioService);
				List<CompletableFuture<Map<String, Object>>> results = paradigm
					.runAllAsync(RotateImageXY.class, parametersList);
				futureIDs = paradigm.getIDs(results);
				List<CompletableFuture<Map<String, Object>>> resultFuture = paradigm
					.getByIDs(futureIDs);

				Dataset ds = (Dataset) resultFuture.get(0).get().get(
					"dataset");
				uiService.show(ds);
			}
		}
	}

	static Object rotateSingleDataset( DatasetIOService ioService, ParallelizationParadigm paradigm )
	{
		List< Map< String, Object > > parametersList = initParameters(ioService);
		List<Map<String, Object>> results = paradigm.runAll(RotateImageXY.class,
				parametersList);
		return results.get( 0 ).get( "dataset" );
	}

	private static List< Map< String, Object > > initParameters( DatasetIOService ioService )
	{
		try
		{
			Dataset dataset = ioService.open( ExampleImage.lenaAsTempFile().toString());
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("dataset", dataset);
			parameters.put("angle", 90);
			return Collections.singletonList(parameters);
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}
}
