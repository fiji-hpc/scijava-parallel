/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.internal;

import java.util.List;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.service.SciJavaService;

/**
 * Type of a service that is responsible for a management of
 * {@link ParadigmManager}.
 * 
 * @author Jan Kožusznik
 */
public interface ParadigmManagerService extends SciJavaService {

	List<ParadigmManager> getManagers(
		Class<? extends ParallelizationParadigm> paradigmType);

	default ParadigmManager getManagers(ParallelizationParadigmProfile profile) {
		return getManagers(profile.getParadigmType()).stream().filter(m -> m
			.isProfileSupported(profile)).findAny().orElse(null);
	}

}
