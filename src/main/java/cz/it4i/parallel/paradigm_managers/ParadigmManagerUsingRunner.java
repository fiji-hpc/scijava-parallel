package cz.it4i.parallel.paradigm_managers;

import java.util.function.Consumer;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;

import cz.it4i.parallel.internal.AbstractBaseRPCParadigmImpl;


public abstract class ParadigmManagerUsingRunner<T extends ParallelizationParadigm, S extends RunnerSettings>
	extends ParadigmManagerWithSettings<S>
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

	@Override
	public final ParallelizationParadigmProfile createProfile(String name) {

		ParallelizationParadigmProfile result = new ParadigmProfileUsingRunner<>(
			getTypeOfRunner(),
			getSupportedParadigmType(), name);
		pluginService.getContext().inject(result);
		return result;
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

}
