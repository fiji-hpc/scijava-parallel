
package cz.it4i.parallel;

import java.util.List;

public interface ServerRunner {

	void start();

	void shutdown();

	List<Integer> getPorts();

	int getNCores();

	void close();


}
