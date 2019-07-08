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

	private final String host;

	private final Integer port;

	private final String userName;

	private final File keyFile;

	private final String keyFilePassword;
	
	private String authenticationChoice;
	
	private String password;

	private final String remoteDirectory;

	private final String command;

	private final int nodes;

	private final int ncpus;

	private String jobID;

	private final HPCSchedulerType adapterType;

	private final boolean shutdownOnClose;

	private final boolean redirectStdInErr;

	@Builder
	private HPCSettings(String host, Integer portNumber, String userName, String authenticationChoice, 
		String password, File keyFile, String keyFilePassword, String remoteDirectory, String command, 
		int nodes, int ncpus, String jobID, HPCSchedulerType adapterType,
		boolean shutdownOnClose, boolean redirectStdInErr)
	{
		this.host = host;
		this.port = portNumber != null ? portNumber : 22;
		this.userName = userName;
		this.authenticationChoice = authenticationChoice;
		this.password = password;
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
		this.redirectStdInErr = redirectStdInErr;
	}


}
