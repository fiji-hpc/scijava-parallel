/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.paradigm_managers;

import java.io.PrintStream;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

@Plugin(type = Service.class, priority = Priority.LOW)
public class ConsoleRedirectingOutputService extends
	BaseRedirectingOutputService
{

	@Override
	public void writeOutput(String output, OutputType outputType) {
		@SuppressWarnings("resource")
		PrintStream outputStream = outputType == OutputType.OUTPUT ? System.out
			: System.err;
		outputStream.print(output);
	}


}
