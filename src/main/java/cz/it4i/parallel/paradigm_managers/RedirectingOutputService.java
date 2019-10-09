/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.paradigm_managers;

import org.scijava.service.SciJavaService;

public interface RedirectingOutputService extends SciJavaService {

	public enum OutputType {
			OUTPUT, ERROR
	}

	void writeOutput(String output, OutputType outputType);

	void registerOutputSource(OutputSource outputSource);

	void unregisterOutputSource(OutputSource outputSource);

	void startAcceptOutput();
}
