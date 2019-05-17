
package cz.it4i.parallel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractImageJServerRunner implements AutoCloseable, ServerRunner {

	private final static Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.AbstractImageJServerRunner.class);

	private final static List<String> IMAGEJ_SERVER_PARAMETERS = Arrays.asList(
		"-Dimagej.legacy.modernOnlyCommands=true", "--", "--ij2", "--headless",
		"--server");

	private final boolean shutdownOnClose;

	@Override
	public void start() {

		try {
			doStartImageJServer();
			getPorts().parallelStream().forEach( this::waitForImageJServer );
		}
		catch (IOException exc) {
			log.error("start imageJServer", exc);
			throw new RuntimeException(exc);
		}
	}

	@Override
	public abstract List<Integer> getPorts();

	@Override
	public abstract List<Integer> getNCores();

	@Override
	public void close() {
		if (shutdownOnClose) {
			shutdown();
		}
	}

	protected List<String> getParameters() {
		return IMAGEJ_SERVER_PARAMETERS;
	}

	protected abstract void doStartImageJServer()
		throws IOException;

	protected void waitForImageJServer(Integer port)
	{
		WaitForHTTPServerRunTS.create("http://localhost:" + port + "/modules")
			.run();
	}

}
