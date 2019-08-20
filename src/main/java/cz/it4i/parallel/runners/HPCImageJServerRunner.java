
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

public class HPCImageJServerRunner extends
	AbstractImageJServerRunner<HPCSettings> implements
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
	public HPCImageJServerRunner init(HPCSettings aSettings) {
		this.settings = aSettings;
		super.init(aSettings);
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
		reconnectIfNeeded();
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
	public Class<HPCSettings> getTypeOfSettings() {
		return HPCSettings.class;
	}


	@Override
	protected void doCloseInternally(boolean shutdown) {
		reconnectIfNeeded();
		super.doCloseInternally(shutdown);
		job = null;
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
		String userName, AuthenticationChoice authenticationChoice, String password,
		String keyFile, String keyFilePassword, HPCSchedulerType adapterType,
		boolean redirectStdInErr) throws JSchException
	{
		if (authenticationChoice == AuthenticationChoice.PASSWORD) {
			return ClusterJobLauncher.createWithPasswordAuthentication(host, port,
				userName, password, adapterType, redirectStdInErr);
		}
		return ClusterJobLauncher.createWithKeyAuthentication(host, port,
			userName, keyFile, keyFilePassword, adapterType, redirectStdInErr);
	}

	private void reconnectIfNeeded() {
		if (job == null && getStatus() == Status.ACTIVE) {
			startOrReconnectServer(this::reconnectServerIfRunningOrDisconnect);
		}
	}

	private void reconnectServerIfRunningOrDisconnect() {
		if (launcher.isJobRunning(settings.getJobID())) {
			job = launcher.getSubmittedJob(settings.getJobID());
		}
		else {
			settings.setJobID(null);
			job = null;
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
			settings.setJobID(job.getID());
		}

	}

}
