// TODO: Add copyright stuff

package org.scijava.parallel;

import java.io.Serializable;

/**
 * A ParallelizationParadigmProfile object encapsulates user-specific
 * information which is used with a given {@link ParallelizationParadigm}. This
 * would typically include user name, password, host address or port number.
 *
 * @author Petr Bainar
 */
public class ParallelizationParadigmProfile
	implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5198616777775710034L;

	/** Profile name */
	private final String profileName;

	/** The {@link ParallelizationParadigm} type to be used in this profile */
	private final Class<? extends ParallelizationParadigm> paradigmType;

	/** A flag determining whether this profile has been selected by the user */
	private Boolean selected;

	public ParallelizationParadigmProfile(
		final Class<? extends ParallelizationParadigm> paradigmType,
		final String profileName)
	{
		this.paradigmType = paradigmType;
		this.profileName = profileName;
	}

	/**
	 * Gets the {@link ParallelizationParadigm} type which is to be used in this
	 * profile
	 * 
	 * @return {@link Class} of given {@link ParallelizationParadigm} 
	 */
	public Class<? extends ParallelizationParadigm> getParadigmType() {
		if (ParallelizationParadigm.class.isAssignableFrom(paradigmType)) {
			return paradigmType;
		}
		return null;
	}

	/**
	 * Returns the {@link #selected} flag
	 */
	public Boolean isSelected() {
		return selected;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Returns {@link #profileName}
	 */
	String getName() {
		return profileName;
	}

	/**
	 * Sets the {@link #selected} flag
	 */
	void setSelected(final Boolean selected) {
		this.selected = selected;
	}
}
