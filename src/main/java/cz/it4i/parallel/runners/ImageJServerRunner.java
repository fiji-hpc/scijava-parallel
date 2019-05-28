
package cz.it4i.parallel.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cz.it4i.parallel.RunningRemoteServer;

public class ImageJServerRunner extends AbstractImageJServerRunner implements
	RunningRemoteServer
{

	private Process imageJServerProcess;

	private String fijiExecutable;

	public ImageJServerRunner(String fiji, boolean shutdownOnClose) {
		super(shutdownOnClose);
		fijiExecutable = fiji;
	}

	@Override
	public void shutdown() {
		if (imageJServerProcess != null) {
			imageJServerProcess.destroy();
		}
	}

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
	public List<Integer> getPorts() {
		return Collections.singletonList(8080);
	}

	@Override
	public List<Integer> getRemotePorts() {
		return getPorts();
	}

	@Override
	protected void doStartImageJServer() throws IOException {
		String fijiPath = fijiExecutable;
		if (fijiPath == null || !Files.exists(Paths.get(fijiPath))) {
			throw new IllegalArgumentException(
				"Cannot find the specified ImageJ or Fiji executable (" + fijiPath +
					"). The property 'Fiji.executable.path' may not be configured properly in the 'configuration.properties' file.");
		}

		List<String> command = Stream.concat(Stream.of(fijiPath), getParameters()
			.stream()).collect(Collectors.toList());

		final ProcessBuilder pb = new ProcessBuilder(command).inheritIO();
		imageJServerProcess = pb.start();

	}

}
