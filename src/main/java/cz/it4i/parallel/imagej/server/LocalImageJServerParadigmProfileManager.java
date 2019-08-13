package cz.it4i.parallel.imagej.server;

import org.scijava.Context;
import org.scijava.parallel.HavingOwnerWindow;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.ImageJServerRunner;
import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ServerRunner;
import javafx.stage.Window;

@Plugin(type = ParadigmManager.class)
public class LocalImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm, ImageJServerRunnerSettings>
	implements HavingOwnerWindow<Window>
{
	@Parameter
	private Context context;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}
	
	@Override
	public Class<Window> getType() {
		return Window.class;
	}

	@Override
	protected Class<? extends ServerRunner<ImageJServerRunnerSettings>>
		getTypeOfRunner()
	{
		return ImageJServerRunner.class;
	}

	@Override
	public void setOwner(Window parent) {
		// will be implemented
	}
}
