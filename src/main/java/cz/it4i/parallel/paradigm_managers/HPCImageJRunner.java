
package cz.it4i.parallel.paradigm_managers;

import com.jcraft.jsch.JSchException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.parallel.Status;
import org.scijava.plugin.Parameter;

import cz.it4i.cluster_job_launcher.ClusterJobLauncher;
import cz.it4i.cluster_job_launcher.ClusterJobLauncher.Job;
import cz.it4i.parallel.internal.InternalExceptionRoutines;
import cz.it4i.parallel.internal.persistence.RunningRemoteServer;

public class HPCImageJRunner extends
	AbstractImageJRunner<HPCSettings> implements
	RunningRemoteServer
{

	private List<Integer> ports = Collections.emptyList();

	private HPCSettings settings;

	private Job job;

	private ClusterJobLauncher launcher;

	private final int startPort;

	@Parameter
	private Context context;

	public HPCImageJRunner(List<String> parameters, IntConsumer portWaiting,
		int startPort)
	{
		super(parameters, portWaiting);
		this.startPort = startPort;
	}

	@Override
	public HPCImageJRunner init(HPCSettings aSettings) {
		this.settings = aSettings;
		super.init(aSettings);
		return this;
	}

	public Job getJob() {
		return job;
	}

	@Override
	public List<Integer> getNCores() {
		return getRemoteHosts().stream().map(x -> settings.getNcpus()).collect(
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
		return ports.stream().map(x -> getStartPort()).collect(Collectors.toList());
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

	protected void doReconnect() {
		startOrReconnectServer(this::reconnectServerIfRunningOrDisconnect);
	}

	@Override
	protected void doStartServer() throws IOException {
		startOrReconnectServer(this::startNewServer);
	}

	private int getStartPort() {
		return startPort;
	}

	@Override
	protected void shutdown() {
		if (job != null) {
			job.stop();
		}
		job = null;
		settings.setJobID(null);
	}

	private ClusterJobLauncher createClusterJobLauncher() throws JSchException {
		if (settings.getAuthenticationChoice() == AuthenticationChoice.PASSWORD) {
			return ClusterJobLauncher.createWithPasswordAuthentication(settings
				.getHost(), settings.getPort(), settings.getUserName(), settings
					.getPassword(), settings.getAdapterType(), settings
						.isRedirectStdInErr());
		}
		return ClusterJobLauncher.createWithKeyAuthentication(settings.getHost(),
			settings.getPort(), settings.getUserName(), settings.getKeyFile()
				.toString(), settings.getKeyFilePassword(), settings.getAdapterType(),
			settings.isRedirectStdInErr());
	}

	private void reconnectIfNeeded() {
		if (job == null && getStatus() == Status.ACTIVE) {
			doReconnect();
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

		launcher = InternalExceptionRoutines.supplyWithExceptionHandling(
			this::createClusterJobLauncher);
		context.inject(launcher);
		command.run();
		if (job != null) {
			ports = job.createTunnels(getStartPort(), getStartPort());
			settings.setJobID(job.getID());
		}

	}

}
