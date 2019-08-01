// TODO: Add copyright stuff

package org.scijava.parallel;

import java.util.List;

import org.scijava.plugin.SingletonService;
import org.scijava.prefs.PrefService;
import org.scijava.service.SciJavaService;

/**
 * A service providing parallelization capabilities
 *
 * @author TODO: Add authors
 */
public interface ParallelService extends
	SingletonService<ParallelizationParadigm>, SciJavaService
{

	/**
	 * Returns an instance of the parallelization paradigm corresponding to the
	 * chosen profile, if available
	 * 
	 * @return Instance of the corresponding parallelization paradigm
	 */
	public ParallelizationParadigm getParadigm();

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
