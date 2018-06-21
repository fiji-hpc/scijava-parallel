package org.scijava.parallel;

import java.io.Serializable;

public class ParallelizationParadigmProfile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final ParallelizationParadigm paradigm;
	
	private final String name;

	public ParallelizationParadigmProfile(final ParallelizationParadigm paradigm, String name) {
		this.paradigm = paradigm;
		this.name = name;
	}
	
	/** Gets the module class described by this {@link ParallelizationParadigm}. */
	public Class<? extends ParallelizationParadigm> getParadigmClass() {
		return paradigm.getClass();
	}
	
}
