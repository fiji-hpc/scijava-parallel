
package cz.it4i.parallel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractImageJServerRunner implements AutoCloseable {

	private final static Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.AbstractImageJServerRunner.class);

	public final static List<String> IMAGEJ_SERVER_PARAMETERS = Arrays.asList(
		"-Dimagej.legacy.modernOnlyCommands=true", "--", "--ij2", "--headless",
		"--server" );

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

	public abstract List<Integer> getPorts();

	@Override
	abstract public void close();

	protected abstract void doStartImageJServer()
		throws IOException;

	private void waitForImageJServer( Integer port )
	{
		boolean running = false;
		while (!running) {
			try {
				running = checkModulesURL(port);
			}
			catch (IOException e) {
				// ignore waiting for start
			}
		}
	}

	private boolean checkModulesURL(Integer port) throws IOException
	{
		HttpURLConnection hc;
		hc = (HttpURLConnection) new URL(getModulesURL(port)).openConnection();
		hc.setRequestMethod("GET");
		hc.connect();
		hc.disconnect();
		return hc.getResponseCode() == 200;
	}

	private String getModulesURL(Integer port) {
		return "http://localhost:" + port + "/modules";
	}

}