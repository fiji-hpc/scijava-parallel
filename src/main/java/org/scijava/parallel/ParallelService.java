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
public interface ParallelService extends SingletonService<ParallelizationParadigm>, SciJavaService {

	// TODO: Consider adding configuration parameters to filter the available paradigms 
	
	/**
	 * Gets all available parallelization paradigms
	 * 
	 * @return A list of available parallelization paradigms
	 */
	List<ParallelizationParadigm> getParadigms();

	/**
	 * Returns an instance of the desired parallelization paradigm, if it is available
	 * @param Class of the desired parallelization paradigm
	 * @return Instance of the desired parallelization paradigm
	 */
	<T extends ParallelizationParadigm> T getParadigm(Class<T> desiredParalellizationParadigm);
	
	/**
	 * Saves the given {@link ParallelizationParadigmProfile} using the {@link PrefService}.
	 */
	public void saveProfile(final ParallelizationParadigmProfile profile);
	
	// -- PTService methods -- 
	
	default Class<ParallelizationParadigm> getPluginType() {
		return ParallelizationParadigm.class;
	}
}
