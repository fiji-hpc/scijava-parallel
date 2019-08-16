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

	private static final long serialVersionUID = -3983910892928520135L;

	@Getter
	@Setter
	private T settings;

	@Getter
	private final Class<? extends ServerRunner<T>> typeOfRunner;

	@Getter
	private transient ServerRunner<T> associatedRunner;

	public ParadigmProfileUsingRunner(
		Class<? extends ServerRunner<T>> typeOfRunner,
		Class<? extends ParallelizationParadigm> paradigmType,
		String profileName)
	{
		super(paradigmType, profileName);
		this.typeOfRunner = typeOfRunner;
	}

	public void disposeRunner() {
		associatedRunner = null;
	}

	Class<T> getTypeOfSettings() {
		if (associatedRunner != null) {
			return associatedRunner.getTypeOfSettings();
		}
		else if (settings != null) {
			@SuppressWarnings("unchecked")
			Class<T> result = (Class<T>) settings.getClass();
			return result;
		}

		return createInstanceOfRunner().getTypeOfSettings();
	}

	void initRunnerIfNeeded(Consumer<ServerRunner<T>> initializer) {
		if (associatedRunner != null) {
			return;
		}
		associatedRunner = createInstanceOfRunner();
		initializer.accept(associatedRunner);
		associatedRunner.init(settings);
	}

	private ServerRunner<T> createInstanceOfRunner() {
		try {
			return typeOfRunner.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc)
		{
			throw new SciJavaParallelRuntimeException(exc);
		}
	}

}
