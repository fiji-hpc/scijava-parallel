package cz.it4i.parallel.imagej.server;

import java.util.Map;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.HPCImageJServerRunner;
import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.parallel.runners.RunnerSettings;
import cz.it4i.parallel.runners.ServerRunner;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.ui.HPCSettingsGui;

@Plugin(type = ParadigmManager.class)
public class HPCImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm>
{

	@Parameter
	private Context context;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}

	@Override
	protected RunnerSettings editSettings(RunnerSettings settings) {
		HPCSettings typedSetttings = null;
		if (settings instanceof HPCSettings) {
			typedSetttings = (HPCSettings) settings;
		}
		RunnerSettings result = super.editSettings(settings);
		if (typedSetttings != null) {
			((HPCSettings) result).setJobID(typedSetttings.getJobID());
		}
		return result;
	}

	@Override
	protected RunnerSettings doEdit(Map<String, Object> inputs) {
		return HPCSettingsGui.showDialog(context, inputs);
	}

	@Override
	protected void fillInputs(RunnerSettings settings,
		Map<String, Object> inputs)
	{
		HPCSettingsGui.fillInputs((HPCSettings) settings, inputs);
	}


	@Override
	protected Class<? extends ServerRunner> getTypeOfRunner() {
		return HPCImageJServerRunnerWithUI.class;
	}

	@Override
	protected void initParadigm(ParadigmProfileUsingRunner typedProfile,
		ImageJServerParadigm paradigm)
	{
		super.initParadigm(typedProfile, paradigm);
		((HPCSettings) typedProfile.getSettings()).setJobID(
			((HPCImageJServerRunner) typedProfile.getAssociatedRunner()).getJob()
				.getID());
	}
}
