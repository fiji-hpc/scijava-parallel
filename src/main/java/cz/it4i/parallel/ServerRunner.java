
package cz.it4i.parallel;

import java.util.List;

public interface ServerRunner {

	void start();

	List<Integer> getPorts();

	int getNCores();

	void close();

}
