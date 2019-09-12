/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/

package org.scijava.parallel;

import java.util.List;

import org.scijava.plugin.SingletonService;
import org.scijava.prefs.PrefService;
import org.scijava.service.SciJavaService;

/**
 * A service providing parallelization capabilities
 *
 * @author Petr Bainar, Jan Kožusznik
 */
public interface ParallelService extends
	SingletonService<ParallelizationParadigm>, SciJavaService
{

	/**
	 * Returns an instance of the parallelization paradigm corresponding to the
	 * chosen profile and type, if available
	 * 
	 * @return Instance of the corresponding parallelization paradigm
	 */
	public <T extends ParallelizationParadigm> T getParadigmOfType(Class<T> type);

	/**
	 * Returns all saved parallelization paradigm profiles
	 * 
	 * @return List of {@link ParallelizationParadigmProfile}
	 */
	public List<ParallelizationParadigmProfile> getProfiles();

	/**
	 * Saves the given {@link ParallelizationParadigmProfile} using the
	 * {@link PrefService}
	 */
	public void addProfile(final ParallelizationParadigmProfile profile);

	/**
	 * Selects the given {@link ParallelizationParadigmProfile}
	 * 
	 * @param name of the {@link ParallelizationParadigmProfile} to be selected
	 */
	public void selectProfile(final String name);

	/**
	 * Deletes the given {@link ParallelizationParadigmProfile}
	 * 
	 * @param name of the {@link ParallelizationParadigmProfile} to be deleted
	 */
	public void deleteProfile(final String name);

	public void deleteProfiles();

	// -- PTService methods --

	@Override
	default Class<ParallelizationParadigm> getPluginType() {
		return ParallelizationParadigm.class;
	}

	void saveProfiles();
}
