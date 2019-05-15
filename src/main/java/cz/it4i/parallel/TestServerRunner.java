package cz.it4i.parallel;

import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestServerRunner implements ServerRunner {

	private ServerRunner serverRunner;

	@Override
	public void start() {
		serverRunner.start();
	}

	@Override
	public List<Integer> getPorts() {
		return serverRunner.getPorts();
	}

	@Override
	public int getNCores() {
		return serverRunner.getNCores();
	}

	@Override
	public void close() {
		serverRunner.close();
	}

	@Override
	public void shutdown() {
		serverRunner.shutdown();
	}

}
