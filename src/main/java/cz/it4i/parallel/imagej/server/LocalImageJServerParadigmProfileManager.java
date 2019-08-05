package cz.it4i.parallel.imagej.server;

import java.util.Map;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.ImageJServerRunner;
import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ServerRunner;
import cz.it4i.parallel.ui.LocalSettingsScreenController;

@Plugin(type = ParadigmManager.class)
public class LocalImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm, ImageJServerRunnerSettings>
{
	// Added this line bellow:
	private LocalSettingsScreenController localSettingsScreenController = new LocalSettingsScreenController();
	
	@Parameter
	private Context context;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}

	@Override
	protected ImageJServerRunnerSettings doEdit(Map<String, Object> inputs) {
		return localSettingsScreenController.showDialog(inputs);
	}

	@Override
	protected void fillInputs(ImageJServerRunnerSettings settings,
		Map<String, Object> inputs)
	{
		localSettingsScreenController.fillInputs(settings, inputs);
	}


	@Override
	protected Class<? extends ServerRunner<ImageJServerRunnerSettings>>
		getTypeOfRunner()
	{
		return ImageJServerRunner.class;
	}
}
