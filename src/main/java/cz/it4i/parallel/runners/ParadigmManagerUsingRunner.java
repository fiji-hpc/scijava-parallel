package cz.it4i.parallel.runners;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;

import cz.it4i.parallel.AbstractBaseParadigm;


public abstract class ParadigmManagerUsingRunner<T extends AbstractBaseParadigm, S extends RunnerSettings>
	implements
	ParadigmManager
{

	@SuppressWarnings("unchecked")
	protected static <C> void runForObjectIfOfTypeElseException(Object profile,
		Class<C> type, Consumer<C> consume)
	{
		if (type.isInstance(profile)) {
			consume.accept((C) profile);
		}
		else {
			throw new UnsupportedOperationException("Not supported for profile: " +
				profile);
		}
	}

	@Override
	public final ParallelizationParadigmProfile createProfile(String name) {
		return new ParadigmProfileUsingRunner<>(getTypeOfRunner(),
			getSupportedParadigmType(), name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editProfile(ParallelizationParadigmProfile profile) {
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class,
			typedProfile -> ((ParadigmProfileUsingRunner<S>) typedProfile)
				.setSettings(editSettings(((ParadigmProfileUsingRunner<S>) typedProfile)
					.getSettings())));
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
			}
		});
	}

	@Override
	public String toString() {
		return "" + getTypeOfRunner().getSimpleName();
	}

	protected S editSettings(S settings) {
		Map<String, Object> inputs = new HashMap<>();
		if (settings != null) {
			fillInputs(settings, inputs);
		}
		return doEdit(inputs);
	}

	protected void initRunner(
		@SuppressWarnings("unused") ServerRunner<?> runner)
	{
		// initialy do nothing
	}

	/**
	 * @param inputs
	 */
	protected S doEdit(Map<String, Object> inputs)
	{
		return null;
	}

	/**
	 * @param settings
	 * @param inputs
	 */
	protected void fillInputs(S settings,
		Map<String, Object> inputs)
	{
		// no needed settings
	}

	protected abstract Class<? extends ServerRunner<S>> getTypeOfRunner();

	protected abstract void initParadigm(
		ParadigmProfileUsingRunner<S> typedProfile,
		T paradigm);

	@SuppressWarnings("unchecked")
	private void prepareParadigmInternal(
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
	}
}
