package cz.it4i.parallel.runners;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;

import cz.it4i.parallel.SciJavaParallelRuntimeException;
import lombok.Getter;
import lombok.Setter;

public class ParadigmProfileUsingRunner<T extends RunnerSettings> extends
	ParallelizationParadigmProfile
{

	public ParadigmProfileUsingRunner(
		Class<? extends ServerRunner<T>> typeOfRunner,
		Class<? extends ParallelizationParadigm> paradigmType,
		String profileName)
	{
		super(paradigmType, profileName);
		this.typeOfRunner = typeOfRunner;
	}

	@Getter
	@Setter
	private T settings;

	@Getter
	private final Class<? extends ServerRunner<T>> typeOfRunner;

	@Getter
	private transient ServerRunner<T> associatedRunner;

	void initRunnerIfNeeded(Consumer<ServerRunner<T>> initializer) {
		if (associatedRunner != null) {
			return;
		}
		try {
			associatedRunner = typeOfRunner.getConstructor().newInstance();
			initializer.accept(associatedRunner);
			associatedRunner.init(settings);
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc)
		{
			throw new SciJavaParallelRuntimeException(exc);
		}
	}

}
