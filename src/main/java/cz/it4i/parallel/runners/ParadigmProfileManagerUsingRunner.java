package cz.it4i.parallel.runners;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;

import cz.it4i.parallel.AbstractBaseParadigm;


public abstract class ParadigmProfileManagerUsingRunner<T extends AbstractBaseParadigm>
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
	public void editProfile(ParallelizationParadigmProfile profile) {
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class, typedProfile -> typedProfile
				.setSettings(editSettings(typedProfile.getSettings())));
	}

	@Override
	public boolean isProfileSupported(ParallelizationParadigmProfile profile) {
		if (getSupportedParadigmType().equals(profile.getParadigmType()) &&
			profile instanceof ParadigmProfileUsingRunner)
		{
			ParadigmProfileUsingRunner typedProfile =
				(ParadigmProfileUsingRunner) profile;
			return typedProfile.getTypeOfRunner().equals(getTypeOfRunner());
		}
		return false;
	}

	@Override
	public void prepareParadigm(
		ParallelizationParadigmProfile profile, ParallelizationParadigm paradigm)
	{
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class,
			typedProfile -> prepareParadigmInternal(typedProfile,
				(AbstractBaseParadigm) paradigm));
	}

	@Override
	public void shutdownIfPossible(ParallelizationParadigmProfile profile)
	{
		runForObjectIfOfTypeElseException(profile,
			ParadigmProfileUsingRunner.class, typedProfile -> {
			ServerRunner runner = typedProfile.getAssociatedRunner();
			if (runner != null && runner.getStatus() == Status.ACTIVE) {
				runner.letShutdownOnClose();
			}
		});
	}


	protected RunnerSettings editSettings(RunnerSettings settings) {
		Map<String, Object> inputs = new HashMap<>();
		if (settings != null) {
			fillInputs(settings, inputs);
		}
		return doEdit(inputs);
	}

	protected abstract RunnerSettings doEdit(Map<String, Object> inputs);

	protected abstract void fillInputs(RunnerSettings settings,
		Map<String, Object> inputs);

	protected abstract Class<?> getTypeOfRunner();

	protected abstract void initParadigm(ParadigmProfileUsingRunner typedProfile,
		T paradigm);

	@SuppressWarnings("unchecked")
	private void prepareParadigmInternal(
		ParadigmProfileUsingRunner typedProfile, AbstractBaseParadigm paradigm)
	{
		typedProfile.initRunnerIfNeeded();
		ServerRunner runner = typedProfile.getAssociatedRunner();
		paradigm.setInitCommand(() -> {
			if (runner.getStatus() == Status.NON_ACTIVE) {
				runner.start();
			}
			initParadigm(typedProfile, (T) paradigm);
		});
		paradigm.setCloseCommand(runner::close);
	}
}
