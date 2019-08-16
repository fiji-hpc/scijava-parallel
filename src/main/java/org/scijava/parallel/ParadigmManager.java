/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/
package org.scijava.parallel;

import org.scijava.plugin.SingletonPlugin;

/**
 * Objects of this type can manipulate profiles for a specific
 * {@link ParallelizationParadigm} type.
 * 
 * @author Jan Kožusznik
 */
public interface ParadigmManager
	extends SingletonPlugin
{

	/**
	 * @return type that
	 */
	Class<? extends ParallelizationParadigm> getSupportedParadigmType();

	boolean isProfileSupported(ParallelizationParadigmProfile profile);


	ParallelizationParadigmProfile createProfile(
		String name);

	boolean editProfile(ParallelizationParadigmProfile profile);

	void prepareParadigm(ParallelizationParadigmProfile profile,
		ParallelizationParadigm paradigm);

	void shutdownIfPossible(ParallelizationParadigmProfile profile);
}
