
package cz.it4i.parallel.runners;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import cz.it4i.parallel.Routines;
import cz.it4i.parallel.RunningRemoteServer;
import cz.it4i.parallel.runners.ClusterJobLauncher.Job;

public class HPCImageJServerRunner extends AbstractImageJServerRunner implements
	RunningRemoteServer
{

	private List< Integer > ports;

	private final HPCSettings settings;

	private Job job;

	private ClusterJobLauncher launcher;

	public HPCImageJServerRunner(HPCSettings settings) {
		this(settings, settings.isShutdownOnClose());
	}

	public HPCImageJServerRunner(HPCSettings settings, boolean shutdownOnClose)
	{
		super(shutdownOnClose);
		this.settings = settings;
		this.ports = Collections.emptyList();
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
	public void shutdown() {
		if (job != null) {
			job.stop();
		}
	}

	@Override
	public void close() {
		super.close();
		launcher.close();
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
	protected void doStartImageJServer() throws IOException {
		launcher = Routines.supplyWithExceptionHandling(
			() -> new ClusterJobLauncher(settings.getHost(), settings.getPort(),
				settings.getUserName(), settings.getKeyFile().toString(), settings
					.getKeyFilePassword(), settings.getAdapterType(), settings
						.isRedirectStdInErr()));
		final String arguments = getParameters().stream().collect(Collectors
			.joining(" "));
		if (settings.getJobID() != null) {
			job = launcher.getSubmittedJob(settings.getJobID());
		}
		else {
			job = launcher.submit(settings.getRemoteDirectory(), settings
				.getCommand(), arguments, settings.getNodes(), settings.getNcpus());
		}
		ports = job.createTunnels(getStartPort(), getStartPort());
	}

	protected int getStartPort() {
		return 8080;
	}


}
