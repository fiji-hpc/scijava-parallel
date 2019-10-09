
package cz.it4i.parallel.paradigm_managers;

import java.util.List;

import org.scijava.parallel.Status;

public interface ServerRunner<T extends RunnerSettings> {

	ServerRunner<T> init(T settings);

	default Class<T> getTypeOfSettings() {
		return null;
	}

	void start();

	void letShutdownOnClose();

	List<Integer> getPorts();

	List<Integer> getNCores();

	void close();

	Status getStatus();
}
