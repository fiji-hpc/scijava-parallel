package org.scijava.parallel;

import java.io.Serializable;

public class ParallelizationParadigmProfile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final Class<? extends ParallelizationParadigm> paradigm;
	
	private final String name;
	
	private Boolean selected;
	
	public String getName() {
		return name;
	}
	
	public Boolean isSelected() {
		return selected;
	}
	
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public ParallelizationParadigmProfile(final Class<? extends ParallelizationParadigm> paradigm, String name) {
		this.paradigm = paradigm;
		this.name = name;
	}
	
	/** Gets the module class described by this {@link ParallelizationParadigm}. */
	@SuppressWarnings("unchecked")
	public <T extends ParallelizationParadigm> Class<T> getParadigmClass() {
		if (ParallelizationParadigm.class.isAssignableFrom(paradigm)) {
			return (Class<T>) paradigm;
		}
		return null;
	}
	
}
