package cz.it4i.parallel.runners;

import java.lang.reflect.InvocationTargetException;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;

import cz.it4i.parallel.SciJavaParallelRuntimeException;
import lombok.Getter;
import lombok.Setter;

public class ParadigmProfileUsingRunner extends
	ParallelizationParadigmProfile
{

	public ParadigmProfileUsingRunner(Class<? extends ServerRunner> typeOfRunner,
		Class<? extends ParallelizationParadigm> paradigmType,
		String profileName)
	{
		super(paradigmType, profileName);
		this.typeOfRunner = typeOfRunner;
	}

	@Getter
	@Setter
	private RunnerSettings settings;

	@Getter
	private final Class<? extends ServerRunner> typeOfRunner;

	@Getter
	private transient ServerRunner associatedRunner;

	void initRunnerIfNeeded() {
		if (associatedRunner != null) {
			return;
		}
		try {
			associatedRunner = typeOfRunner.getConstructor().newInstance();
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
