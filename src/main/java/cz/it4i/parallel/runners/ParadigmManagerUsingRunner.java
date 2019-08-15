package cz.it4i.parallel.runners;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

import cz.it4i.parallel.AbstractBaseParadigm;


public abstract class ParadigmManagerUsingRunner<T extends AbstractBaseParadigm, S extends RunnerSettings>
	implements
	ParadigmManager
{

	@Parameter
	private PluginService pluginService;

	@SuppressWarnings("unchecked")
	protected static <C> Boolean runForObjectIfOfTypeElseException(Object profile,
		Class<C> type, Predicate<C> predicate)
	{
		Boolean correct = false;
		
		if (type.isInstance(profile)) {
			correct = predicate.test((C) profile);
		}
		else {
			throw new UnsupportedOperationException("Not supported for profile: " +
				profile);
		}
		
		return correct;
	}

	@Override
	public final ParallelizationParadigmProfile createProfile(String name) {
		return new ParadigmProfileUsingRunner<>(getTypeOfRunner(),
			getSupportedParadigmType(), name);
	}

	@Override
	public Boolean editProfile(ParallelizationParadigmProfile profile) {
		Boolean correct = false;
		correct = runForObjectIfOfTypeElseException(profile, 
			ParadigmProfileUsingRunner.class,
			this::editSettings);
		
		return correct;
	}

	@Override
	public boolean isProfileSupported(ParallelizationParadigmProfile profile) {
		if (getSupportedParadigmType().equals(profile.getParadigmType()) &&
			profile instanceof ParadigmProfileUsingRunner)
		{
			@SuppressWarnings("unchecked")
			ParadigmProfileUsingRunner<S> typedProfile =
				(ParadigmProfileUsingRunner<S>) profile;
			return typedProfile.getTypeOfRunner().equals(getTypeOfRunner());
		}
		return false;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void prepareParadigm(
		ParallelizationParadigmProfile profile, ParallelizationParadigm paradigm)
	{
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class,
			typedProfile -> prepareParadigmInternal(
				typedProfile,
				(AbstractBaseParadigm) paradigm));
	}

	@Override
	public void shutdownIfPossible(ParallelizationParadigmProfile profile)
	{
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class, typedProfile -> {
				ServerRunner<?> runner = typedProfile.getAssociatedRunner();
			if (runner != null && runner.getStatus() == Status.ACTIVE) {
				runner.letShutdownOnClose();
				return false;
			}
			return false;
		});
	}

	@Override
	public String toString() {
		return "" + getTypeOfRunner().getSimpleName();
	}

	protected Boolean editSettings(
		ParadigmProfileUsingRunner<S> typedProfile)
	{
		Boolean correct = false;
		RunnerSettingsEditor<S> editor = getEditor(typedProfile
			.getTypeOfSettings());
		typedProfile.setSettings(editor.edit(typedProfile.getSettings()));
		
		if(typedProfile.getSettings() != null) {
			correct = true;
		}
		return correct;
	}

	protected void initRunner(
		@SuppressWarnings("unused") ServerRunner<?> runner)
	{
		// initialy do nothing
	}

	protected abstract Class<? extends ServerRunner<S>> getTypeOfRunner();

	protected abstract void initParadigm(
		ParadigmProfileUsingRunner<S> typedProfile,
		T paradigm);

	@SuppressWarnings("unchecked")
	private Boolean prepareParadigmInternal(
		ParadigmProfileUsingRunner<S> typedProfile, AbstractBaseParadigm paradigm)
	{
		typedProfile.initRunnerIfNeeded(this::initRunner);
		ServerRunner<?> runner = typedProfile.getAssociatedRunner();
		paradigm.setInitCommand(() -> {
			if (runner.getStatus() == Status.NON_ACTIVE) {
				runner.start();
			}
			initParadigm(typedProfile, (T) paradigm);
		});
		paradigm.setCloseCommand(runner::close);
		return false;		
	}

	private RunnerSettingsEditor<S> getEditor(
		Class<S> clazz)
	{

		@SuppressWarnings("unchecked")
		RunnerSettingsEditor<S> result = pluginService.createInstancesOfType(
			RunnerSettingsEditor.class).stream().filter(rse -> rse.getTypeOfSettings()
				.equals(clazz)).findFirst().orElse(
					new RunnerSettingsEditor<S>()
					{
						@Override
						public RunnerSettings edit(RunnerSettings aSettings) {
							return aSettings;
						}

						@Override
						public Class<S> getTypeOfSettings() {
							return clazz;
						}
					});

		return result;
	}
}
