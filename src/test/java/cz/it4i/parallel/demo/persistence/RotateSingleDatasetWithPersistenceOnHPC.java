
package cz.it4i.parallel.demo.persistence;

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

import cz.it4i.parallel.demo.ExampleImage;
import cz.it4i.parallel.persistence.PersistentParallelizationParadigmImpl;
import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.ui.HPCSettingsGui;
import cz.it4i.parallel.utils.TestParadigm;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstration example showing basic usage of {@link PersistentParallelizationParadigm} with
 * ImageJ server started in HPC cluster. It downloads a picture (Lena) and
 * rotate it for 90 degree. Result is immediately showed.
 * 
 * @formatter:off
 * 
 * You can use settings: 
 * 1. Host name: salomon.it4i.cz
 * 2. Remote directory with Fiji: /scratch/work/project/open-15-12/apps/Fiji.app-scijava-parallel
 * 3. Remote ImageJ command: fiji-linux64
 * 4. Number of nodes: 1
 * 5. Number of CPUs per node: 24
 * @formatter:on
 * 
 * @author koz01
 */
@Slf4j
public class RotateSingleDatasetWithPersistenceOnHPC {

	private final static String REQUEST_DATA_FILE = "requestData.obj";

	private final static Path PATH_TO_STORED_REQUEST_DATA_FILE;

	private static Context context;

	private static DatasetIOService ioService;

	private static UIService uiService;

	private static ImageJ ij;

	public static void main(String[] args) throws InterruptedException,
		ExecutionException, ClassNotFoundException, IOException
	{
		initImageJAndSciJava();

		Map<Class<?>, Object> requestData = null;
		if (areRequestDataStored()) {
			requestData = loadRequestData();
		}
		else {
			requestData = new HashMap<>();
		}
		boolean exit = false;
		try (PersistentParallelizationParadigm paradigm = constructParadigm(
			requestData))
		{

			if (!requestData.containsKey(List.class)) {
				List<CompletableFuture<Map<String, Object>>> results = paradigm
					.runAllAsync(RotateImageXY.class, initParameters());
				requestData.put(List.class, paradigm.getIDs(results));
				storeRequestData(requestData);
				exit = true;
			}
			else {
				@SuppressWarnings("unchecked")
				List<CompletableFutureID> futureIDs =
					(List<CompletableFutureID>) requestData.get(List.class);
				CompletableFuture<Map<String, Object>> resultFuture = paradigm.getByIDs(
					futureIDs).get(0);
				Dataset ds = (Dataset) resultFuture.get().get("dataset");
				uiService.show(ds);
				paradigm.purge(futureIDs);
			}

		}
		if (exit) {
			context.dispose();
			uiService.dispose();
			System.exit(0);
		}
		else {
			ij.ui().getDefaultUI().dispose();
		}

	}

	private static PersistentParallelizationParadigm constructParadigm(
		Map<Class<?>, Object> requestData)
	{
		HPCSettings settings = (HPCSettings) requestData.get(HPCSettings.class);
		boolean shutDownOnClose = true;
		if (settings != null) {
			shutDownOnClose &= settings.isShutdownOnClose();
		}
		else {
			shutDownOnClose = false;
			settings = HPCSettingsGui.showDialog(context);
			requestData.put(HPCSettings.class, settings);
		}
		final HPCSettings finalHpcSettings = settings;

		final HPCImageJServerRunnerWithUI runner = new HPCImageJServerRunnerWithUI(
			finalHpcSettings, shutDownOnClose)
		{

			@Override
			public void start() {
				super.start();
				finalHpcSettings.setJobID(this.getJob().getID());
			}
		};
		TestParadigm innerParadigm = new TestParadigm(runner, context);
		return PersistentParallelizationParadigmImpl.addPersistencyToParadigm(
			innerParadigm, runner, innerParadigm.getParadigm().getClass()
				.getCanonicalName());
	}

	private static void initImageJAndSciJava() {
		ij = new ImageJ();
		ij.ui().showUI();
		context = ij.getContext();
		ioService = context.service(DatasetIOService.class);
		uiService = context.service(UIService.class);
	}

	static {
		PATH_TO_STORED_REQUEST_DATA_FILE = Paths.get(REQUEST_DATA_FILE);
	}

	private static boolean areRequestDataStored() {
		return Files.exists(PATH_TO_STORED_REQUEST_DATA_FILE);
	}

	@SuppressWarnings("unchecked")
	private static Map<Class<?>, Object> loadRequestData() throws IOException,
		ClassNotFoundException
	{
		Map<Class<?>, Object> result = null;
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(
			PATH_TO_STORED_REQUEST_DATA_FILE)))
		{
			result = (Map<Class<?>, Object>) ois.readObject();
		}
		Files.delete(PATH_TO_STORED_REQUEST_DATA_FILE);
		return result;
	}

	private static void storeRequestData(Map<Class<?>, Object> requestData)
		throws IOException
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
			PATH_TO_STORED_REQUEST_DATA_FILE)))
		{
			oos.writeObject(requestData);
		}
	}

	private static List<Map<String, Object>> initParameters() {
		try {
			Dataset dataset = ioService.open(ExampleImage.lenaAsTempFile()
				.toString());
			log.info("input dataset: " + getName(dataset));
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("dataset", dataset);
			parameters.put("angle", 90);
			return Collections.singletonList(parameters);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getName(Object dataset) {
		return dataset.getClass().toString() + System.identityHashCode(dataset);
	}

}
