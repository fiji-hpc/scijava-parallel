package cz.it4i.parallel.runners;

import java.util.List;

import cz.it4i.fiji.scpclient.SshCommandClient;

public interface HPCSchedulerAdapter {
	String getSpawnCommand();

	String constructSubmitCommand(long nodes, long ncpus, String string);

	void waitForStart(SshCommandClient client, String jobId);

	List<String> getNodes(SshCommandClient client, String jobId);

	String getOutputFileName(String jobId, String suffix);

	boolean isOutErrTogether();

	void stop(SshCommandClient client, String jobId);
}
