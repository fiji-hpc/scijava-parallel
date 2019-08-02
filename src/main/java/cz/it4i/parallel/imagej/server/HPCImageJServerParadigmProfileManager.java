package cz.it4i.parallel.imagej.server;

import java.awt.Window;
import java.util.Map;

import org.scijava.Context;
import org.scijava.parallel.HavingParentWindows;
import org.scijava.parallel.ParadigmManager;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.runners.HPCImageJServerRunner;
import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.parallel.runners.MultipleHostsParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.parallel.runners.ServerRunner;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.ui.HPCSettingsGui;
import cz.it4i.parallel.ui.HPCSettingsScreenWindow;


@Plugin(type = ParadigmManager.class)
public class HPCImageJServerParadigmProfileManager extends
	MultipleHostsParadigmManagerUsingRunner<ImageJServerParadigm, HPCSettings>
	implements HavingParentWindows<Window>
{

	private HPCSettingsScreenWindow hpcSettingsScreenWindow = new HPCSettingsScreenWindow(); 
	
	@Parameter
	private Context context;
	private Window parent;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}

	@Override
	public void initParent(Window aParent) {
		this.parent = aParent;
	}

	@Override
	public Class<Window> getType() {
		return Window.class;
	}

	@Override
	protected HPCSettings editSettings(HPCSettings settings) {
		HPCSettings result = super.editSettings(settings);
		if (settings != null) {
			result.setJobID(settings.getJobID());
		}
		return result;
	}

	@Override
	protected HPCSettings doEdit(HPCSettings settings) {
		return hpcSettingsScreenWindow.showDialog(settings);
	}

	@Override
	protected void fillInputs(HPCSettings settings,
		Map<String, Object> inputs)
	{
		HPCSettingsGui.fillInputs(settings, inputs);
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
		typedRunner.initParentWindow(parent);
	}
}
