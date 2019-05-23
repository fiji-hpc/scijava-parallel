
package cz.it4i.parallel.demo.hpc;

import static cz.it4i.parallel.Routines.runWithExceptionHandling;

import com.google.common.collect.Streams;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.plugins.commands.imglib.RotateImageXY;

import org.scijava.parallel.ParallelizationParadigm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.parallel.AbstractImageJServerRunner;
import cz.it4i.parallel.demo.RotateFile;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.utils.TestParadigm;

/**
 * Demonstration example showing basic usage of ParalellizationParadigm with
 * ImageJ server started on HPC cluster. It downloads a picture (Lena) and
 * rotate it for 170 and 340 degree. Result is stored into directory output
 * located in working directory. Result is processed asynchronously.
 * 
 * @author koz01
 */
public class RotateFileAsyncOnHPC {

	private final static Logger log = LoggerFactory.getLogger(
		RotateFileAsyncOnHPC.class);
	private static DatasetIOService ioService;

	public static void main(String[] args) throws IOException {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		ioService = ij.getContext().service(DatasetIOService.class);
		AbstractImageJServerRunner runner = HPCImageJServerRunnerWithUI.gui(ij
			.context());
		try (ParallelizationParadigm paradigm = new TestParadigm(runner, ij
			.context()))
		{
			List<Map<String, Object>> parametersList = RotateFile.initParameters(
				ioService);
			List< CompletableFuture< Map< String, Object > > > results = paradigm.runAllAsync(
					RotateImageXY.class, parametersList );
			asyncSaveOutputs( parametersList, results );
		}
	}

	private static void asyncSaveOutputs( List< Map< String, Object > > parametersList, List< CompletableFuture< Map< String, Object > > > results )
	{
		// @formatter:off
		Path outputDirectory = RotateFile.prepareOutputDirectory();
		Streams.zip(results.stream(), parametersList.stream().map(
			inputParams -> (Double) inputParams.get("angle")),
			(future, angle) -> future.thenAccept(
				result -> {
					Path dst = outputDirectory.resolve("result_" + angle + ".png");
					runWithExceptionHandling(() -> ioService.save((Dataset) result.get(
							"dataset"), dst.toString()));
					}))
		.forEach(future -> waitForFuture(future));
		// @formatter:on
	}

	private static void waitForFuture( CompletableFuture< Void > future ) {
		try {
			future.get();
		}
		catch (InterruptedException | ExecutionException exc) {
			log.error("wait for completition", exc);
		}
	}
}
