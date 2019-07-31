
package cz.it4i.parallel.runners;

import com.jcraft.jsch.JSchException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.scpclient.SshCommandClient;
import cz.it4i.fiji.scpclient.SshExecutionSession;
import cz.it4i.parallel.runners.ClusterJobLauncher.Job.POutThread;

public class ClusterJobLauncher implements Closeable {

	private final static Logger log = LoggerFactory.getLogger(
		ClusterJobLauncher.class);

	private final static long REMOTE_CONSOLE_READ_TIMEOUT = 500;

	private Collection<POutThread> threads = new LinkedList<>();

	public class Job {

		/*
		 * stdout accesible ssh <> ssh <node> tail -f -n +1 /var/spool/PBS/spool/<job id>.OU 
		 */

		private String jobId;

		private CompletableFuture<List<String>> nodesFuture;

		private boolean redirectOut;

		private Job(String jobId, boolean openOut) {
			super();
			this.jobId = jobId;
			this.redirectOut = openOut;
			nodesFuture = CompletableFuture.supplyAsync(this::getNodesFromServer);
		}

		public CompletableFuture<List<String>> getNodesFuture() {
			return nodesFuture;
		}

		public List<String> getNodes() {
			try {
				return nodesFuture.get();
			}
			catch (InterruptedException | ExecutionException exc) {
				throw new RuntimeException(exc);
			}
		}

		public List<Integer> createTunnels(int startPort, int remotePort) {
			List<Integer> result = new LinkedList<>();
			for (String host : getNodes()) {
				boolean opened;
				do {
					opened = client.setPortForwarding(startPort, host, remotePort);
					if (opened) {
						result.add(startPort);
					}
					startPort++;
				}
				while (!opened);
			}
			return result;
		}

		public CompletableFuture<List<String>> runCommandOnNode(int nodeNumber,
			String command)
		{
			String node = getNodes().get(nodeNumber);
			return CompletableFuture.supplyAsync(() -> client.executeCommand("ssh " +
				node + " " + command));
		}

		public void createTunnel(int localPort, String host, int remotePort) {
			client.setPortForwarding(localPort, host, remotePort);
		}

		public void stop() {
			adapter.stop(client, jobId);
		}

		public String getID() {
			return jobId;
		}

		private void waitForRunning() {

			if (jobId == null) {
				throw new IllegalStateException("jobId not initialized");
			}

			adapter.waitForStart(client, jobId);
			if (redirectOut) {
				startOutThread(System.out, "OU");
				if (!adapter.isOutErrTogether()) {
					startOutThread(System.err, "ER");
				}
			}
		}

		private List<String> getNodesFromServer() {
			waitForRunning();
			if (jobId == null) {
				throw new IllegalStateException("jobId not initialized");
			}
			return adapter.getNodes(client, jobId);
		}

		private void startOutThread(PrintStream out, String suffix) {
			POutThread thread = new POutThread(out, suffix);
			threads.add(thread);
			thread.start();
		
		}

		class POutThread extends Thread

		{

			private OutputStream outputStream;
			private String suffix;
			private SshExecutionSession usedSession;

			public POutThread(OutputStream outputStream, String suffix) {
				super();
				this.outputStream = outputStream;
				this.suffix = suffix;
			}

			@Override
			public void run() {
				try (SshExecutionSession session = (usedSession = client
					.openSshExecutionSession(
						"ssh -t " + getNodes().get(0) + " tail -f -n +1 " + adapter
							.getOutputFileName(jobId, suffix), true)))
				{
					byte[] buffer = new byte[1024];
					int readed;
					InputStream is = session.getStdout();
					while (-1 != (readed = is.read(buffer))) {
						outputStream.write(buffer, 0, readed);
						sleepForWhile(REMOTE_CONSOLE_READ_TIMEOUT);
					}
				}
				catch (IOException exc) {
					log.error(exc.getMessage(), exc);
				}
			}

			@Override
			public void interrupt() {
				usedSession.close();
				super.interrupt();
			}
		}
	}

	private SshCommandClient client;

	private HPCSchedulerBridge adapter;

	private boolean redirectStdOutErr;

	public static ClusterJobLauncher createWithKeyAuthentication(String hostName,
		int port, String userName, String keyLocation,
		String keyPassword, HPCSchedulerType hpcSchedulerType,
		boolean redirectStdOutErr) throws JSchException
	{
		return new ClusterJobLauncher(new SshCommandClient(hostName, userName,
			keyLocation, keyPassword), port, hpcSchedulerType, redirectStdOutErr);
	}

	public static ClusterJobLauncher createWithPasswordAuthentication(
		String hostName,
		int port, String userName, String password,
		HPCSchedulerType hpcSchedulerType, boolean redirectStdOutErr)

	{
		return new ClusterJobLauncher(new SshCommandClient(hostName, userName,
			password), port, hpcSchedulerType, redirectStdOutErr);
	}

	private ClusterJobLauncher(SshCommandClient client, int port,
		HPCSchedulerType hpcSchedulerType, boolean redirectStdOutErr)
	{
		super();

		this.client = client;
		this.client.setPort(port);
		this.adapter = hpcSchedulerType.create();
		this.redirectStdOutErr = redirectStdOutErr;
	}

	public Job submit(String directory, String command, String parameters,
		long usedNodes, long ncpus)
	{
		String jobId = runJob(directory, command, parameters, usedNodes, ncpus);
		return new Job(jobId, redirectStdOutErr);
	}

	public Job getSubmittedJob(String jobId) {
		return new Job(jobId, redirectStdOutErr);
	}

	public boolean isJobRunning(String jobID) {
		return adapter.isJobRunning(client, jobID);
	}

	@Override
	public void close() {
		threads.forEach(POutThread::interrupt);
		this.client.close();
	}

	private String runJob(String directory, String command, String parameters,
		long nodes, long ncpus)
	{
		String jobname = java.time.Instant.now().toEpochMilli() + "";
		String fileName = jobname + ".sh";
// @formatter:off
		String result = client.executeCommand(
			"echo '" + 
			"#!/usr/bin/env bash\n" +
			"cd "+directory+"\n" +
			adapter.getSpawnCommand() + " `readlink -f "+command+"` " + parameters + "\n" +
			"/usr/bin/tail -f /dev/null' > " + fileName + " && " +
			"chmod +x " + fileName +" && " +
			adapter.constructSubmitCommand(nodes, ncpus, "`readlink -f " + fileName + "`")).get(0);
// @formatter:on
		client.executeCommand("rm " + fileName);
		return result;
	}

	static void sleepForWhile(long timeout) {
		try {
			Thread.sleep(timeout);
		}
		catch (InterruptedException exc) {}
	}

}
