package cz.it4i.parallel.runners;

import static cz.it4i.parallel.runners.ClusterJobLauncher.sleepForWhile;

import java.util.List;

import cz.it4i.fiji.scpclient.SshCommandClient;

class SlurmHPCSchedulerBridge implements HPCSchedulerBridge {

	private static final String DIRECTORY_FOR_OUT = "~/.scijava-parallel";
	@Override
	public String getSpawnCommand() {
		return "srun";
	}

	@Override
	public String constructSubmitCommand(long nodes, long ncpus, String command) {
		return String.format(
			"if [ ! -d %s ]; then mkdir %1$s; fi && sbatch --parsable --nodes=%2$d --mincpus=%3$d --output=`readlink -f %1$s`/%%j.OU --error=`readlink -f %1$s`/%%j.ER %4$s",
			DIRECTORY_FOR_OUT, nodes, ncpus, command);
	}

	@Override
	public void waitForStart(SshCommandClient client, String jobId) {
		String state;
		String time;
		do {
			String result = client.executeCommand("squeue --format '%M %t' --job " +
				jobId).get(1);
			String[] tokens = result.split(" +");
			time = tokens[0];
			state = tokens[1];
			sleepForWhile(1000);
		}
		while (!(!time.equals("0") && state.equals("R")));
	}

	@Override
	public List<String> getNodes(SshCommandClient client, String jobId) {
		String result = client.executeCommand("squeue --format '%N' --job " + jobId)
			.get(1);
		return splitNodeNames(client, result);
	}

	@Override
	public String getOutputFileName(String jobId, String suffix) {
		return DIRECTORY_FOR_OUT + "/" + jobId + "." + suffix;
	}

	@Override
	public boolean isOutErrTogether() {
		return false;
	}

	@Override
	public boolean isJobRunning(SshCommandClient client, String jobID) {
		String result = client.executeCommand("squeue --format '%M %t' --job " +
			jobID).get(1);
		String[] tokens = result.split(" +");
		String state = tokens[1];
		return state.equals("R");
	}

	@Override
	public void stop(SshCommandClient client, String jobId) {
		client.executeCommand("scancel " + jobId);
	}

	private static List<String> splitNodeNames(SshCommandClient client,
		String result)
	{
		return client.executeCommand("scontrol show hostnames '" + result + "'");
	}

}
