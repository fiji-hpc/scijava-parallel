package cz.it4i.parallel.runners;

import java.io.File;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class HPCSettings implements Serializable
{

	private String host;

	private Integer port;

	private String userName;

	private File keyFile;

	private String keyFilePassword;

	private String remoteDirectory;

	private String command;

	private int nodes;

	private int ncpus;

	private String jobID;

	private HPCSchedulerType adapterType;

	private boolean shutdownOnClose;

	@SuppressWarnings("unused")
	private HPCSettings() {
	}

	@Builder
	private HPCSettings(String host, Integer portNumber, String userName,
		File keyFile,
		String keyFilePassword, String remoteDirectory, String command, int nodes,
		int ncpus, String jobID, HPCSchedulerType adapterType,
		boolean shutdownOnClose)
	{
		this.host = host;
		this.port = portNumber != null ? portNumber : 22;
		this.userName = userName;
		this.keyFile = keyFile;
		this.keyFilePassword = keyFilePassword;
		this.remoteDirectory = remoteDirectory;
		this.command = command;
		this.nodes = nodes;
		this.ncpus = ncpus;
		this.jobID = jobID;
		this.adapterType = adapterType != null ? adapterType
			: HPCSchedulerType.SLURM;
		this.shutdownOnClose = shutdownOnClose;
	}


}
