package cz.it4i.parallel.paradigm_managers;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.scijava.Context;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;

import cz.it4i.parallel.SciJavaParallelRuntimeException;
import lombok.Getter;

public class ParadigmProfileUsingRunner<T extends RunnerSettings> extends
	ParadigmProfileWithSettings<T>
{

	private static final long serialVersionUID = -3983910892928520135L;



	@Getter
	private final Class<? extends ServerRunner<T>> typeOfRunner;

	@Getter
	private transient ServerRunner<T> associatedRunner;

	@Parameter
	private transient Context ctx;


	public ParadigmProfileUsingRunner(
		Class<? extends ServerRunner<T>> typeOfRunner,
		Class<? extends ParallelizationParadigm> paradigmType,
		String profileName)
	{
		super(paradigmType, profileName);
		this.typeOfRunner = typeOfRunner;
	}

	public void setShutdownOnParadigmClose()
	{
		ServerRunner<?> runner = getAssociatedRunner();
		if (runner != null && runner.getStatus() == Status.ACTIVE) {
			runner.letShutdownOnClose();
		}
	}

	@Override
	protected Class<T> getTypeOfSettings() {
		Class<T> result;
		if (associatedRunner != null) {
			result = associatedRunner.getTypeOfSettings();
		}
		else {

			result = super.getTypeOfSettings();
			
		}
		if (result == null) {
			result = createInstanceOfRunner().getTypeOfSettings();
		}
		return result;
	}

	void prepareRunner(Consumer<ServerRunner<T>> initializer) {
		if (associatedRunner == null) {
			associatedRunner = createInstanceOfRunner();
		}
		initializer.accept(associatedRunner);
		associatedRunner.init(getSettings());
	}

	private ServerRunner<T> createInstanceOfRunner() {
		try {
			ServerRunner<T> result = typeOfRunner.getConstructor().newInstance();
			ctx.inject(result);
			return result;
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc)
		{
			throw new SciJavaParallelRuntimeException(exc);
		}
	}

}
