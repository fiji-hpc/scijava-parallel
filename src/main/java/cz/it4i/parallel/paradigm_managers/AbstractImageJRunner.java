
package cz.it4i.parallel.paradigm_managers;

import java.io.IOException;
import java.util.List;
import java.util.function.IntConsumer;

import cz.it4i.cluster_job_launcher.CJLauncherRuntimeException;
import lombok.AccessLevel;
import lombok.Setter;

abstract class AbstractImageJRunner<T extends RunnerSettings>
	implements AutoCloseable, ServerRunner<T>
{

	

	@Setter(value = AccessLevel.PROTECTED)
	private boolean shutdownOnClose;

	private boolean shutdownOnNextClose;

	private final List<String> parameters;

	private final IntConsumer portWaiting;


	public AbstractImageJRunner(List<String> parameters,
		IntConsumer portWaiting)
	{
		super();
		this.parameters = parameters;
		this.portWaiting = portWaiting;
	}

	@Override
	public ServerRunner<T> init(T settings) {
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
			throw new CJLauncherRuntimeException(exc);
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

	protected final List<String> getParameters() {
		return parameters;
	}

	protected abstract void doStartServer()
		throws IOException;

	protected abstract void shutdown();

	private void waitForServer(Integer port) {
		portWaiting.accept(port);
	}

}
