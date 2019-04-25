
package test;

import static cz.it4i.parallel.Routines.runWithExceptionHandling;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imagej.Dataset;
import net.imagej.plugins.commands.imglib.RotateImageXY;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.Routines;
import cz.it4i.parallel.TestParadigm;

public class RotateFile {

	private static final String OUTPUT_DIRECTORY = "output";

	private final static int step = 170;

	private static DatasetIOService ioService;

	public static void main(String[] args) throws IOException {
		final Context context = new Context();
		ioService = context.service(DatasetIOService.class);
		try ( ParallelizationParadigm paradigm = TestParadigm.localImageJServer( Config.getFijiExecutable(), context ) ) {
			callRemotePlugin(paradigm);
		}
	}

	static void callRemotePlugin(final ParallelizationParadigm paradigm)
		throws IOException
	{
		if (ioService == null) {
			ioService = new Context().getService(DatasetIOService.class);
		}
		final List<Map<String, Object>> parametersList = initParameters(ioService);
		final List<Map<String, Object>> results = paradigm.runAll(
				RotateImageXY.class, parametersList);
		saveOutputs( parametersList, results );
	}

	static List<Map<String, Object>> initParameters(
		DatasetIOService ioServiceLocal)
		throws IOException
	{
		final List<Map<String, Object>> parametersList = new LinkedList<>();
		Dataset dataset = ioServiceLocal.open(ExampleImage.lenaAsTempFile()
			.toString());
		for (double angle = step; angle < 360; angle += step) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("dataset", dataset);
			parameters.put("angle", angle);
			parametersList.add(parameters);
		}
		return parametersList;
	}

	private static void saveOutputs( List< Map< String, Object > > parametersList, List< Map< String, Object > > results )
	{
		final Path outputDirectory = prepareOutputDirectory();
		final Iterator<Map<String, Object>> inputIterator = parametersList
				.iterator();
		for (Map<String, ?> result : results) {
			final Double angle = ( Double ) inputIterator.next().get( "angle" );
			final Path outputFile = outputDirectory.resolve("result_" + angle +
				".png");
			runWithExceptionHandling(() -> ioService.save((Dataset) result.get(
				"dataset"), outputFile.toString()));
		}
	}

	static Path prepareOutputDirectory() {
		Path outputDirectory = Paths.get(OUTPUT_DIRECTORY);
		if (!Files.exists(outputDirectory)) {
			Routines.runWithExceptionHandling(() -> Files.createDirectories(
				outputDirectory));
		}
		return outputDirectory;
	}
}
