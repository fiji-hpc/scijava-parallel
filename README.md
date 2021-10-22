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
