package cz.it4i.parallel.imagej.server;

import java.util.Collections;
import java.util.Map;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.Host;
import cz.it4i.parallel.runners.ImageJServerRunner;
import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import cz.it4i.parallel.runners.ParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.parallel.runners.RunnerSettings;
import cz.it4i.parallel.ui.ImageJSettingsGui;

@Plugin(type = ParadigmManager.class)
public class LocalImageJServerParadigmProfileManager extends
	ParadigmManagerUsingRunner<ImageJServerParadigm>
{

	@Parameter
	private Context context;

	@Override
	public Class<ImageJServerParadigm> getSupportedParadigmType() {
		return ImageJServerParadigm.class;
	}


	@Override
	public ParallelizationParadigmProfile createProfile(
		String name)
	{
		return new ParadigmProfileUsingRunner(ImageJServerRunner.class,
			ImageJServerParadigm.class, name);
	}

	@Override
	protected RunnerSettings doEdit(Map<String, Object> inputs) {
		return ImageJSettingsGui.showDialog(context, inputs);
	}

	@Override
	protected void fillInputs(RunnerSettings settings,
		Map<String, Object> inputs)
	{
		ImageJSettingsGui.fillInputs((ImageJServerRunnerSettings) settings, inputs);
	}


	@Override
	protected Class<?> getTypeOfRunner() {
		return ImageJServerRunner.class;
	}

	@Override
	protected void initParadigm(ParadigmProfileUsingRunner typedProfile,
		ImageJServerParadigm paradigm)
	{
		paradigm.setHosts(Collections.singletonList(new Host("localhost:" +
			typedProfile.getAssociatedRunner().getPorts().get(0), Runtime.getRuntime()
				.availableProcessors())));
	}
}
