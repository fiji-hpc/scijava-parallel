
package cz.it4i.parallel.runners;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.scijava.parallel.Status;

import cz.it4i.parallel.internal.persistence.RunningRemoteServer;

public class LocalImageJRunner extends
	AbstractImageJRunner<LocalImageJRunnerSettings> implements
	RunningRemoteServer
{

	public LocalImageJRunner(List<String> parameters, IntConsumer portWaiting,
		int portNumber)
	{
		super(parameters, portWaiting);
		this.portNumber = portNumber;
	}

	private Process imageJServerProcess;

	private String fijiExecutable;

	private final int portNumber;

	@Override
	public List<Integer> getNCores() {
		return Collections.singletonList(Runtime.getRuntime()
			.availableProcessors());
	}

	@Override
	public List<String> getRemoteHosts() {
		return Collections.singletonList("localhost");
	}

	@Override
	public final List<Integer> getPorts() {
		return Collections.singletonList(portNumber);
	}

	@Override
	public List<Integer> getRemotePorts() {
		return getPorts();
	}

	@Override
	public Status getStatus() {
		return imageJServerProcess == null ? Status.NON_ACTIVE : Status.ACTIVE;
	}

	@Override
	public Class<LocalImageJRunnerSettings> getTypeOfSettings() {
		return LocalImageJRunnerSettings.class;
	}

	@Override
	public LocalImageJRunner init(LocalImageJRunnerSettings settings) {
		super.init(settings);
		fijiExecutable = settings.getFijiExecutable();
		return this;
	}

	@Override
	protected void doStartServer() throws IOException {
		String fijiPath = fijiExecutable;
		if (fijiPath == null || !Paths.get(fijiPath).toFile().exists()) {
			throw new IllegalArgumentException(
				"Cannot find the specified ImageJ or Fiji executable (" + fijiPath +
					"). The property 'Fiji.executable.path' may not be configured properly in the 'configuration.properties' file.");
		}

		List<String> command = Stream.concat(Stream.of(fijiPath), getParameters()
			.stream()).collect(Collectors.toList());

		final ProcessBuilder pb = new ProcessBuilder(command).inheritIO();
		imageJServerProcess = pb.start();

	}

	@Override
	protected void shutdown() {
		if (imageJServerProcess != null) {
			imageJServerProcess.destroy();
			imageJServerProcess = null;
		}
	}

}
