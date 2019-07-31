
package cz.it4i.parallel.runners;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import cz.it4i.parallel.SciJavaParallelRuntimeException;
import lombok.AccessLevel;
import lombok.Setter;

public abstract class AbstractImageJServerRunner implements AutoCloseable,
	ServerRunner
{

	private static final List<String> IMAGEJ_SERVER_PARAMETERS = Arrays.asList(
		"-Dimagej.legacy.modernOnlyCommands=true", "--", "--ij2", "--headless",
		"--server");

	@Setter(value = AccessLevel.PROTECTED)
	private boolean shutdownOnClose;

	private boolean shutdownOnNextClose;


	@Override
	public ServerRunner init(RunnerSettings settings) {
		shutdownOnClose = settings.isShutdownOnClose();
		return this;
	}

	@Override
	public void start() {

		try {
			doStartServer();
			getPorts().parallelStream().forEach(this::waitForServer);
		}
		catch (IOException exc) {
			throw new SciJavaParallelRuntimeException(exc);
		}
	}

	@Override
	public final synchronized void close() {
		doCloseInternally(shutdownOnClose || shutdownOnNextClose);
		shutdownOnNextClose = false;
	}

	@Override
	public final synchronized void letShutdownOnClose() {
		shutdownOnNextClose = true;
	}

	protected void doCloseInternally(boolean shutdown) {
		if (shutdown) {
			shutdown();
		}
	}

	protected List<String> getParameters() {
		return IMAGEJ_SERVER_PARAMETERS;
	}

	protected abstract void doStartServer()
		throws IOException;

	protected abstract void shutdown();

	protected void waitForServer(Integer port)
	{
		WaitForHTTPServerRunTS.create("http://localhost:" + port + "/modules")
			.run();
	}

}
