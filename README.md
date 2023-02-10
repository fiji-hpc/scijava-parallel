# scijava-parallel
A project aiming to utilize parallelization within SciJava

## Use remote Fiji as worker

Fiji that could be used as worker should contain libraries from update site of ImageJ server (Server) and these (in jars folder):
* httpclient-4.5.2.jar 
* httpcore-4.4.4.jar 
* httpmime-4.3.1.jar
* json-simple-1.1.jar

-------
This repository holds implementation of parallelization profiles `Parallel Paradigm`, which are also used to connect to clusters.
To edit relevant parameters, the repository also includes a few GUI dialogs.

-------
# Relevant links of interest
The most visible project that benefits from this codebase is the HPC Workflow Manager.
Below are links relevant to it:

- [__The Short Guide on how to install and use the HPC Workflow Manager__](https://github.com/fiji-hpc/parallel-macro/wiki/Short-Guide)
- [The official web page about the whole HPC Workflow Manager](https://fiji-hpc.github.io/hpc-parallel-tools/)
- [Wiki page of the HPC Workflow Manager](https://imagej.net/HPC_Workflow_Manager) on [imagej.net](https://imagej.net/)
