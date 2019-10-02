/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.runners;

import java.util.HashSet;
import java.util.Set;

import org.scijava.service.AbstractService;

public abstract class BaseRedirectingOutputService extends AbstractService
	implements RedirectingOutputService
{

	protected Set<OutputSource> outputSources = new HashSet<>();

	@Override
	public void registerOutputSource(OutputSource outputSource) {
		outputSources.add(outputSource);
	}

	@Override
	public void unregisterOutputSource(OutputSource outputSource) {
		outputSources.remove(outputSource);
	}

	@Override
	public void startAcceptOutput() {
		outputSources.forEach(os -> os.statusOfOutputChanged(true));
	}

}
