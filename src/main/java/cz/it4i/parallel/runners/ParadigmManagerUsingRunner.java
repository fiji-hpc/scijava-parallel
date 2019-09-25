package cz.it4i.parallel.runners;

import java.util.function.Consumer;
import java.util.function.Function;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

import cz.it4i.parallel.AbstractBaseRPCParadigmImpl;


public abstract class ParadigmManagerUsingRunner<T extends AbstractBaseRPCParadigmImpl, S extends RunnerSettings>
	implements
	ParadigmManager
{

	@Parameter
	private PluginService pluginService;

	protected static <C> void runForObjectIfOfTypeElseException(
		ParallelizationParadigmProfile profile,
		Class<C> clazz, Consumer<C> consumer)
	{
		runWithResultForObjectIfOfTypeElseException(profile, clazz, c -> {
			consumer.accept(c);
			return null;
		});
	}

	@SuppressWarnings("unchecked")
	protected static <C, R> R runWithResultForObjectIfOfTypeElseException(
		Object profile, Class<C> type, Function<C, R> predicate)
	{
		if (type.isInstance(profile)) {
			return predicate.apply((C) profile);
		}
		throw new UnsupportedOperationException("Not supported for profile: " +
			profile);
	}

	@Override
	public final ParallelizationParadigmProfile createProfile(String name) {
		return new ParadigmProfileUsingRunner<>(getTypeOfRunner(),
			getSupportedParadigmType(), name);
	}

	@Override
	public boolean editProfile(ParallelizationParadigmProfile profile) {
		return runWithResultForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class, this::editSettings);
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
			typedProfile -> prepareParadigmInternal(typedProfile,
				(AbstractBaseRPCParadigmImpl) paradigm));
	}

	@Override
	public String toString() {
		return "" + getTypeOfRunner().getSimpleName();
	}

	protected boolean editSettings(
		ParadigmProfileUsingRunner<S> typedProfile)
	{
		RunnerSettingsEditor<S> editor = getEditor(typedProfile
			.getTypeOfSettings());
		typedProfile.setSettings(editor.edit(typedProfile.getSettings()));
		
		return typedProfile.getSettings() != null;
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
	private void prepareParadigmInternal(
		ParadigmProfileUsingRunner<S> typedProfile, AbstractBaseRPCParadigmImpl paradigm)
	{
		
		typedProfile.prepareRunner(this::initRunner);
		paradigm.setInitCommand(() -> {
			ServerRunner<?> serverRunner = typedProfile.getAssociatedRunner();
			serverRunner.getPorts();
			if (serverRunner.getStatus() == Status.NON_ACTIVE) {
				serverRunner.start();
			}
			initParadigm(typedProfile, (T) paradigm);
		});

		paradigm.setCloseCommand(() -> {
			ServerRunner<?> serverRunner = typedProfile.getAssociatedRunner();
			if (serverRunner != null) {
				serverRunner.close();
			}
		});
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
