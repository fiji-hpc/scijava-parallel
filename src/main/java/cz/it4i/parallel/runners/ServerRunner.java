
package cz.it4i.parallel.runners;

import java.util.List;

import org.scijava.parallel.Status;

public interface ServerRunner {

	ServerRunner init(RunnerSettings settings);

	void start();

	void letShutdownOnClose();

	List<Integer> getPorts();

	List<Integer> getNCores();

	void close();

	Status getStatus();
}
