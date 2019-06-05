package cz.it4i.parallel.runners;

import static cz.it4i.parallel.runners.ClusterJobLauncher.sleepForWhile;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import cz.it4i.fiji.scpclient.SshCommandClient;

class PBSHPCSchedulerBridge implements HPCSchedulerBridge {

	@Override
	public String getSpawnCommand() {
		return "pbsdsh --";
	}

	@Override
	public String constructSubmitCommand(long nodes, long ncpus, String command) {
		return "qsub  -q qexp -l select=" + nodes + ":ncpus=" + ncpus + " " +
			command;
	}

	@Override
	public void waitForStart(SshCommandClient client, String jobId) {
		String state;
		String time;
		do {
			String result = client.executeCommand("qstat " + jobId).get(2);
			String[] tokens = result.split(" +");
			state = tokens[4];
			time = tokens[3];
			sleepForWhile(1000);
		}
		while (!(!time.equals("0") && state.equals("R")));
	}

	@Override
	public List<String> getNodes(SshCommandClient client, String jobId) {
		List<String> result = client.executeCommand("qstat -f " + jobId);
		List<String> hostLines = new LinkedList<>();
		for (String line : result) {
			if (hostLines.isEmpty() && line.contains("exec_host")) {
				hostLines.add(line);
			}
			else if (!hostLines.isEmpty()) {
				if (!line.contains("exec_vnode")) {
					hostLines.add(line);
				}
				else {
					break;
				}
			}
		}
		result = new LinkedList<>(new LinkedHashSet<>(Arrays.asList(hostLines
			.stream().collect(Collectors.joining("")).replaceAll("\\s+", "")
			.replaceAll("exec_host=", "").replaceAll("/[^+]+", "").split("\\+"))));
		return result;
	}

	@Override
	public String getOutputFileName(String jobId, String suffix) {
		return "/var/spool/PBS/spool/" + jobId + "." + suffix;
	}

	@Override
	public boolean isOutErrTogether() {
		return false;
	}

	@Override
	public void stop(SshCommandClient client, String jobId) {
		client.executeCommand("qdel " + jobId);
	}

}
