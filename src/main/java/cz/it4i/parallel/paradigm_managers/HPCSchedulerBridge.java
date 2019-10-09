package cz.it4i.parallel.paradigm_managers;

import java.util.List;

import cz.it4i.fiji.scpclient.SshCommandClient;

interface HPCSchedulerBridge {
	String getSpawnCommand();

	String constructSubmitCommand(long nodes, long ncpus, String string);

	void waitForStart(SshCommandClient client, String jobId);

	List<String> getNodes(SshCommandClient client, String jobId);

	String getOutputFileName(String jobId, String suffix);

	boolean isOutErrTogether();

	void stop(SshCommandClient client, String jobId);

	boolean isJobRunning(SshCommandClient client, String jobID);
}
