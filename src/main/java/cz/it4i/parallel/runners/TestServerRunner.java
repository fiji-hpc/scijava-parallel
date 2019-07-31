package cz.it4i.parallel.runners;

import java.util.List;

import org.scijava.parallel.Status;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestServerRunner implements ServerRunner
{

	private ServerRunner serverRunner;

	@Override
	public TestServerRunner init(RunnerSettings settings) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void start() {
		serverRunner.start();
	}

	@Override
	public List<Integer> getPorts() {
		return serverRunner.getPorts();
	}

	@Override
	public List<Integer> getNCores() {
		return serverRunner.getNCores();
	}

	@Override
	public Status getStatus() {
		return serverRunner.getStatus();
	}

	@Override
	public void close() {
		serverRunner.close();
	}

	@Override
	public void letShutdownOnClose() {
		serverRunner.letShutdownOnClose();
	}

}
