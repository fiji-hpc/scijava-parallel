
package cz.it4i.parallel.internal.ui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.FileWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.TextWidget;

import cz.it4i.cluster_job_launcher.HPCSchedulerType;
import cz.it4i.parallel.SciJavaParallelRuntimeException;
import cz.it4i.parallel.paradigm_managers.AuthenticationChoice;
import cz.it4i.parallel.paradigm_managers.HPCSettings;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileSettingsEditor;

@Plugin(type = Command.class, headless = false)
public class HPCSettingsGui implements Command
{
	
	@Plugin(type = ParadigmProfileSettingsEditor.class)
	public static class Editor implements ParadigmProfileSettingsEditor<HPCSettings> {

		@Parameter
		private Context context;

		@Override
		public Class<HPCSettings> getTypeOfSettings() {
			return HPCSettings.class;
		}

		@Override
		public HPCSettings edit(HPCSettings aSettings) {
			Map<String, Object> inputs = new HashMap<>();
			if (aSettings != null) {
				fillInputs(aSettings, inputs);
			}
			return showDialog(context, inputs);
		}
	}

	public static void fillInputs(HPCSettings settings,
		Map<String, Object> inputs)
	{
		inputs.put("host", settings.getHost());
		inputs.put("port", settings.getPort());
		inputs.put("userName", settings.getUserName());
		inputs.put("authenticationChoice", settings.getAuthenticationChoice());
		inputs.put("keyFile", settings.getKeyFile());
		inputs.put("keyFilePassword", settings.getKeyFilePassword());
		inputs.put("password", settings.getPassword());
		inputs.put("schedulerType", settings.getAdapterType().toString());
		inputs.put("remoteDirectory", settings.getRemoteDirectory());
		inputs.put("command", settings.getCommand());
		inputs.put("nodes", settings.getNodes());
		inputs.put("ncpus", settings.getNcpus());
		inputs.put("shutdownJobAfterClose", settings.isShutdownOnClose());
		inputs.put("redirectStdOutErr", settings.isRedirectStdInErr());
	}

	public static HPCSettings showDialog(Context context,
		Map<String, Object> inputs)
	{
		CommandService command = context.service(CommandService.class);
		try {
			Future<CommandModule> module;
			if (inputs == null || inputs.isEmpty()) {
				module = command.run(HPCSettingsGui.class, true);
			}
			else {
				module = command.run(HPCSettingsGui.class, true, inputs);
			}

			return (HPCSettings) module.get().getOutput("settings");
		}
		catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new SciJavaParallelRuntimeException(e);
		}
	}

	public static HPCSettings showDialog(Context context) {
		return showDialog(context, null);
	}

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String labelSSH = "SSH Settings";

	@Parameter(style = TextWidget.FIELD_STYLE, label = "Host name")
	private String host;

	@Parameter(style = NumberWidget.SPINNER_STYLE, label = "Port number",
		min = "1", max = "65535")
	private int port = 22;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "User name")
	private String userName;

	@Parameter(label = "Authentication method:",
		style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE, choices = { "Key file",
			"Password" })
	private String authenticationChoice;

	@Parameter(style = FileWidget.OPEN_STYLE, label = "Key file")
	private File keyFile;

	@Parameter(style = TextWidget.PASSWORD_STYLE, label = "Key file password",
		persist = false, required = false)
	private String keyFilePassword;

	@Parameter(style = TextWidget.PASSWORD_STYLE, label = "Password",
		persist = false, required = false)
	private String password;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String labelHPC = "HPC Settings";

	@Parameter(choices = { "PBS", "Slurm" }, label = "HPC Scheduler type")
	private String schedulerType = "PBS";

	// for salomon /scratch/work/project/dd-18-42/apps/fiji-with-server
	@Parameter(style = FileWidget.DIRECTORY_STYLE,
		label = "Remote directory with Fiji")
	private String remoteDirectory;

	@Parameter(style = TextWidget.FIELD_STYLE, label = "Remote ImageJ command")
	private String command = "ImageJ-linux64";

	// for salomon run-workers.sh
	@Parameter(style = NumberWidget.SPINNER_STYLE, label = "Number of nodes",
		min = "1")
	private int nodes;

	@Parameter(style = NumberWidget.SPINNER_STYLE,
		label = "Number of cpus per node", min = "1")
	private int ncpus;

	@Parameter(style = TextWidget.FIELD_STYLE,
		label = "Shutdown job when application finishes.")
	private boolean shutdownJobAfterClose;

	@Parameter(style = TextWidget.FIELD_STYLE,
		label = "Redirect standard output ")
	private boolean redirectStdOutErr;

	@Parameter(type = ItemIO.OUTPUT)
	private HPCSettings settings;

	@Override
	public void run() {
		// Convert the authentication choice from String to Enum:
		AuthenticationChoice authenticationChoiceEnum;
		if (authenticationChoice.equals("Key file") || authenticationChoice.equals("KEY_FILE")) {
			authenticationChoiceEnum = AuthenticationChoice.KEY_FILE;
		}
		else {
			authenticationChoiceEnum = AuthenticationChoice.PASSWORD;
		}
		
		settings = HPCSettings.builder().host(host).portNumber(port).userName(
			userName).authenticationChoice(authenticationChoiceEnum).password(password)
			.keyFile(keyFile).keyFilePassword(keyFilePassword).remoteDirectory(
				remoteDirectory).command(command).nodes(nodes).ncpus(ncpus)
			.shutdownOnClose(shutdownJobAfterClose).redirectStdInErr(
				redirectStdOutErr).adapterType(HPCSchedulerType.getByString(
					schedulerType)).build();
	}

}
