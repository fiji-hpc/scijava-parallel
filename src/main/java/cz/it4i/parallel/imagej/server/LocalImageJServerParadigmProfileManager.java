package cz.it4i.parallel.imagej.server;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.ImageJServerRunner;
import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ServerRunner;
import cz.it4i.parallel.ui.LocalSettingsScreenWindow;

@Plugin(type = ParadigmManager.class)
public class LocalImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm, ImageJServerRunnerSettings>
{
	// Added this line bellow:
	private LocalSettingsScreenWindow localSettingsScreenWindow = new LocalSettingsScreenWindow();
	
	@Parameter
	private Context context;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}
	
	@Override
	protected ImageJServerRunnerSettings doEdit(ImageJServerRunnerSettings settings) {
		return localSettingsScreenWindow.showDialog(settings);
	}

	@Override
	protected Class<? extends ServerRunner<ImageJServerRunnerSettings>>
		getTypeOfRunner()
	{
		return ImageJServerRunner.class;
	}
}
