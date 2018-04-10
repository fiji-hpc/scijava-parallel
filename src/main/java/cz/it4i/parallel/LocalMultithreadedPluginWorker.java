package cz.it4i.parallel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;

public class LocalMultithreadedPluginWorker implements ParallelWorker {

	@Parameter
	private CommandService commandService;

	@Parameter
	private DatasetIOService datasetIOService;

	@Parameter
	private Context context;

	public LocalMultithreadedPluginWorker() {
		new Context().inject(this);
	}

	private final Map<String, String> cachedFilePaths = new WeakHashMap<>();
	private final Map<String, Object> cachedOutputs = new HashMap<>();

	@Override
	public String uploadFile(String filePath, String name) {
		String filePathIdentifier = UUID.randomUUID().toString();
		cachedFilePaths.put(filePathIdentifier, filePath);
		return filePathIdentifier;
	}

	@Override
	public void downloadFile(String id, String filePath) {
		Object retrievedOutput = cachedOutputs.get(id);
		if (Dataset.class.isAssignableFrom(retrievedOutput.getClass())) {
			try {
				datasetIOService.save((Dataset) retrievedOutput, filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public String deleteResource(String id) {
		Object output;
		output = cachedOutputs.remove(id);
		if (output instanceof Dataset) {
			Dataset ds = (Dataset) output;
			ds.decrementReferences();
		}
		return null;
	}

	@Override
	public <T extends Command> Map<String, Object> executeCommand(Class<T> commandType, Map<String, ?> map) {

		// Create a new Object-typed input map
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.putAll(map);

		// Retrieve command and replace GUIDs in inputs where applicable
		CommandInfo commandInfo = commandService.getCommand(commandType);
		if (commandInfo != null) {
			for (final ModuleItem<?> input : commandInfo.inputs()) {
				if (Dataset.class.isAssignableFrom(input.getType())) {
					final Object datasetIdentifier = inputMap.get(input.getName());
					if (datasetIdentifier instanceof String) {
						final String filepath = cachedFilePaths.get(datasetIdentifier);
						try {
							inputMap.replace(input.getName(), datasetIOService.open(filepath));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}

			// Execute command and cache outputs
			Map<String, Object> outputs = null;
			try {
				outputs = commandService.run(commandInfo, true, inputMap).get().getOutputs();

				for (final Entry<String, Object> entry : outputs.entrySet()) {
					String outputIdentifier = UUID.randomUUID().toString();
					cachedOutputs.put(outputIdentifier, entry.getValue());
					
					// As this method is designed for single-output commands, return
					
				}
				return outputs;
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	
	@Override
	public Map<String, String> getCommandArgumentsMap(String commandName) {
		// TODO Auto-generated method stub
		return null;
	}

}