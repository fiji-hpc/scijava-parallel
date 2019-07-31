package org.scijava.parallel;

import org.scijava.plugin.SingletonPlugin;

public interface ParadigmManager
	extends SingletonPlugin
{

	Class<?> getSupportedParadigmType();

	boolean isProfileSupported(ParallelizationParadigmProfile profile);


	ParallelizationParadigmProfile createProfile(
		String name);

	void editProfile(ParallelizationParadigmProfile profile);

	void prepareParadigm(ParallelizationParadigmProfile profile,
		ParallelizationParadigm paradigm);

	void shutdownIfPossible(ParallelizationParadigmProfile profile);
}
