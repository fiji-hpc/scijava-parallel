package cz.it4i.parallel.imagej.server;


import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.HPCImageJServerRunner;
import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.parallel.runners.ServerRunner;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import javafx.stage.Window;


@Plugin(type = ParadigmManager.class)
public class HPCImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm, HPCSettings>
{
	
	@Parameter
	private Context context;

	private Window ownerWindow;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}

	@Override
	protected Boolean editSettings(
		ParadigmProfileUsingRunner<HPCSettings> typedProfile)
	{
		Boolean correct = false;
		
		HPCSettings settings = typedProfile.getSettings();
		correct = super.editSettings(typedProfile);
		if (settings != null) {
			typedProfile.getSettings().setJobID(settings.getJobID());
		}
		
		return correct;
	}

	@Override
	protected Class<? extends ServerRunner<HPCSettings>> getTypeOfRunner() {
		return HPCImageJServerRunnerWithUI.class;
	}

	@Override
	protected void initParadigm(
		ParadigmProfileUsingRunner<HPCSettings> typedProfile,
		ImageJServerParadigm paradigm)
	{
		super.initParadigm(typedProfile, paradigm);
		typedProfile.getSettings().setJobID(
			((HPCImageJServerRunner) typedProfile.getAssociatedRunner()).getJob()
				.getID());
	}

	@Override
	protected void initRunner(ServerRunner<?> runner) {
		HPCImageJServerRunnerWithUI typedRunner =
			(HPCImageJServerRunnerWithUI) runner;
		typedRunner.initOwnerWindow(ownerWindow);
	}
}
