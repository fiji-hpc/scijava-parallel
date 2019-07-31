
package cz.it4i.parallel.ui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;
import org.scijava.widget.TextWidget;

import cz.it4i.parallel.SciJavaParallelRuntimeException;
import cz.it4i.parallel.runners.ImageJServerRunnerSettings;

@Plugin(type = Command.class, headless = false)
public class ImageJSettingsGui implements Command {
	
	public static void fillInputs(ImageJServerRunnerSettings settings,
		Map<String, Object> inputs)
	{
		Path fiji = Paths.get(settings.getFijiExecutable());
		inputs.put("localDirectory", fiji.getParent().toString());
		inputs.put("command", fiji.getFileName().toString());
	}

	@Parameter(style = FileWidget.DIRECTORY_STYLE,
		label = "Local directory with Fiji")
	private File localDirectory;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "ImageJ command")
	private String command = "ImageJ-linux64";

	@Parameter(type = ItemIO.OUTPUT)
	private ImageJServerRunnerSettings settings;

	@Override
	public void run() {
		Path fiji = localDirectory.toPath().resolve(command);
		settings = ImageJServerRunnerSettings.builder().fiji(fiji.toString())
			.build();
	}

	public static ImageJServerRunnerSettings showDialog(Context context,
		Map<String, Object> inputs)
	{
		CommandService command = context.service(CommandService.class);
		try {
			Future<CommandModule> module;
			if (inputs == null || inputs.isEmpty()) {
				module = command.run(ImageJSettingsGui.class, true);
			}
			else {
				module = command.run(ImageJSettingsGui.class, true, inputs);
			}

			return (ImageJServerRunnerSettings) module.get().getOutput("settings");
		}
		catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new SciJavaParallelRuntimeException(e);
		}
	}
}
