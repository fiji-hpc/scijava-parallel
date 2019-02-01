
package cz.it4i.parallel;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractImageJServerRunner implements AutoCloseable {

	private final static Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.AbstractImageJServerRunner.class);

	private static String[] IMAGEJ_SERVER_WITH_PARAMETERS = { "ImageJ-linux64",
		"-Dimagej.legacy.modernOnlyCommands=true", "--", "--ij2", "--headless",
		"--server" };

	private List<Integer> ports;

	public void startIfNecessary() {
		if (!checkImageJServerRunning()) {
			startImageJServer();
		}
	}

	public List<Integer> getPorts() {
		return ports;
	}

	protected void setPorts(List<Integer> ports) {
		this.ports = ports;
	}

	@Override
	abstract public void close();

	protected abstract void doStartImageJServer(List<String> command)
		throws IOException;

	protected void imageJServerStarted() {}

	protected void imageJServerRunning() {}

	private void startImageJServer() {

		try {
			doStartImageJServer(Arrays.asList(IMAGEJ_SERVER_WITH_PARAMETERS));
			imageJServerStarted();
			getPorts().parallelStream().forEach(port -> {
				boolean running = false;
				do {
					try {
						if (checkModulesURL(port) == 200) {
							running = true;
						}
					}
					catch (IOException e) {
						// ignore waiting for start
					}
				}
				while (!running);
			});
			imageJServerRunning();
		}
		catch (IOException exc) {
			log.error("start imageJServer", exc);
			throw new RuntimeException(exc);
		}
	}

	private boolean checkImageJServerRunning() {
		return ports.stream().map(port -> {
			try {
				if (checkModulesURL(port) != 200) {
					throw new IllegalStateException(
						"Different server than ImageJServer is running on localhost:8080");
				}
				return true;
			}
			catch (ConnectException exc) {
				return false;
			}
			catch (IOException exc) {
				log.error("connect ot ImageJServer", exc);
				throw new RuntimeException(exc);
			}
		}).allMatch(result -> result);
	}

	private int checkModulesURL(Integer port) throws IOException,
		MalformedURLException, ProtocolException
	{
		HttpURLConnection hc;
		hc = (HttpURLConnection) new URL(getModulesURL(port)).openConnection();
		hc.setRequestMethod("GET");
		hc.connect();
		hc.disconnect();
		return hc.getResponseCode();
	}

	private String getModulesURL(Integer port) {
		return "http://localhost:" + port + "/modules";
	}

}
