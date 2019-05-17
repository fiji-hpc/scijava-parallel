package cz.it4i.parallel;

import java.util.List;

public interface RunningRemoteServer {

	List<Integer> getRemotePorts();

	List<String> getRemoteHosts();

	List<Integer> getNCores();
}
