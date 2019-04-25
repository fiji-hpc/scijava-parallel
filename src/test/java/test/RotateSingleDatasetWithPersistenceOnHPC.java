
package test;

import io.scif.services.DatasetIOService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.scijava.parallel.PersistentParallelizationParadigm;
import org.scijava.parallel.PersistentParallelizationParadigm.CompletableFutureID;
import org.scijava.ui.UIService;

import cz.it4i.parallel.TestParadigmPersistent;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RotateSingleDatasetWithPersistenceOnHPC
{

	private static Context context;

	private static DatasetIOService ioService;

	private static UIService uiService;

	public static void main(String[] args) throws InterruptedException,
		ExecutionException, ClassNotFoundException, IOException
	{
		initImageJAndSciJava();

		List<CompletableFutureID> futureIDs;
		try (PersistentParallelizationParadigm paradigm = constructParadigm())
		{

			if (areRequestIDSStored()) {
				List<CompletableFuture<Map<String, Object>>> results = paradigm
					.runAllAsync(RotateImageXY.class, initParameters());

				futureIDs = paradigm.getIDs(results);

				storeRequestID(futureIDs);
			}
			else {
				futureIDs = loadRequestID();

				CompletableFuture<Map<String, Object>> resultFuture = paradigm.getByIDs(
					futureIDs).get(0);
				Dataset ds = (Dataset) resultFuture.get().get("dataset");
				uiService.show(ds);

				paradigm.purge(futureIDs);
			}
		}
	

	}

	private static PersistentParallelizationParadigm constructParadigm() {
		final HPCImageJServerRunnerWithUI runner = HPCImageJServerRunnerWithUI.gui(
			context);
		registerShutDownAction(runner);
		return TestParadigmPersistent.runningImageJServer(context, runner, false);

	}

	private static void initImageJAndSciJava() {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		context = ij.getContext();
		ioService = context.service(DatasetIOService.class);
		uiService = context.service(UIService.class);
	}

	private final static String REQUEST_ID_FILE = "requestID.obj";

	private final static Path PATH_TO_STORED_REQUESTIDS_FILE;
	static {
		PATH_TO_STORED_REQUESTIDS_FILE = Paths.get(REQUEST_ID_FILE);
	}

	private static boolean areRequestIDSStored() {
		return !Files.exists(PATH_TO_STORED_REQUESTIDS_FILE);
	}

	@SuppressWarnings("unchecked")
	private static List<CompletableFutureID> loadRequestID() throws IOException,
		ClassNotFoundException
	{
		List<CompletableFutureID> result = null;
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(
			PATH_TO_STORED_REQUESTIDS_FILE)))
		{
			result = (List<CompletableFutureID>) ois.readObject();
		}
		Files.delete(PATH_TO_STORED_REQUESTIDS_FILE);
		return result;
	}

	private static void registerShutDownAction(
		final HPCImageJServerRunnerWithUI runner)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (runner.isShutdownJob()) {
				runner.close();
			}
		}));
	}


	private static void storeRequestID(List<CompletableFutureID> futureIDs)
		throws IOException
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
			PATH_TO_STORED_REQUESTIDS_FILE)))
		{
			oos.writeObject(futureIDs);
		}
	
	}



	private static List<Map<String, Object>> initParameters()
	{
		try
		{
			Dataset dataset = ioService.open( ExampleImage.lenaAsTempFile().toString());
			log.info("input dataset: " + getName(dataset));
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

	private static String getName(Object dataset) {
		return dataset.getClass().toString() + System.identityHashCode(dataset);
	}
}
