
package cz.it4i.parallel.runners;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.parallel.Status;

import cz.it4i.parallel.Routines;
import cz.it4i.parallel.RunningRemoteServer;
import cz.it4i.parallel.runners.ClusterJobLauncher.Job;

public class HPCImageJServerRunner extends AbstractImageJServerRunner implements
	RunningRemoteServer
{

	private List<Integer> ports = Collections.emptyList();

	private HPCSettings settings;

	private Job job;

	private ClusterJobLauncher launcher;

	public HPCImageJServerRunner() {
	}

	public HPCImageJServerRunner(HPCSettings settings) {
		settings = settings.clone();
		init(settings);
	}

	@Override
	public HPCImageJServerRunner init(RunnerSettings aSettings) {
		this.settings = (HPCSettings) aSettings;
		super.init(aSettings);
		if (settings.getJobID() != null) {
			startOrReconnectServer(this::reconnectServerIfRunningOrDisconnect);
		}
		return this;
	}

	public Job getJob() {
		return job;
	}

	@Override
	public List<Integer> getNCores() {
		return getRemoteHosts().stream().map(__ -> settings.getNcpus()).collect(
			Collectors.toList());
	}

	@Override
	public List<String> getRemoteHosts() {
		return getJob().getNodes();
	}

	@Override
	public List< Integer > getPorts()
	{
		return ports;
	}

	@Override
	public List<Integer> getRemotePorts() {
		return ports.stream().map(X -> getStartPort()).collect(Collectors.toList());
	}

	@Override
	public Status getStatus() {
		return settings != null && settings.getJobID() != null ? Status.ACTIVE
			: Status.NON_ACTIVE;
	}

	@Override
	protected void doCloseInternally(boolean shutdown) {
		super.doCloseInternally(shutdown);
		launcher.close();
		launcher = null;
	}

	@Override
	protected void doStartServer() throws IOException {
		startOrReconnectServer(this::startNewServer);
	}

	protected int getStartPort() {
		return 8080;
	}

	@Override
	protected void shutdown() {
		if (job != null) {
			job.stop();
		}
		job = null;
		settings.setJobID(null);
	}

	private ClusterJobLauncher createClusterJobLauncher(String host, Integer port,
		String userName, String authenticationChoice, String password,
		String keyFile, String keyFilePassword, HPCSchedulerType adapterType,
		boolean redirectStdInErr) throws JSchException
	{
		if (authenticationChoice.equals("Password")) {
			return ClusterJobLauncher.createWithPasswordAuthentication(host, port,
				userName, password, adapterType, redirectStdInErr);
		}
		return ClusterJobLauncher.createWithKeyAuthentication(host, port,
			userName, keyFile, keyFilePassword, adapterType, redirectStdInErr);
	}

	private void reconnectServerIfRunningOrDisconnect() {
		if (launcher.isJobRunning(settings.getJobID())) {
			job = launcher.getSubmittedJob(settings.getJobID());
		}
		else {
			settings.setJobID(null);
			launcher.close();
			launcher = null;
		}
	}

	private void startNewServer() {
		final String arguments = getParameters().stream().collect(Collectors
			.joining(" "));
		job = launcher.submit(settings.getRemoteDirectory(), settings.getCommand(),
			arguments, settings.getNodes(), settings.getNcpus());
	}

	private void startOrReconnectServer(Runnable command) {

		launcher = Routines.supplyWithExceptionHandling(
			() -> createClusterJobLauncher(settings.getHost(), settings.getPort(),
				settings.getUserName(), settings.getAuthenticationChoice(), settings.getPassword(), settings.getKeyFile().toString(), settings
					.getKeyFilePassword(), settings.getAdapterType(), settings
						.isRedirectStdInErr()));

		command.run();
		if (job != null) {
			ports = job.createTunnels(getStartPort(), getStartPort());
		}
	}

}
