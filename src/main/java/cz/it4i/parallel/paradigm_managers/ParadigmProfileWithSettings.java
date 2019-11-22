/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.paradigm_managers;

import java.io.Serializable;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;

import lombok.Getter;
import lombok.Setter;

public class ParadigmProfileWithSettings<T extends Serializable> extends
	ParallelizationParadigmProfile
{

	private static final long serialVersionUID = 8826202452459922029L;

	@Getter
	@Setter
	private T settings;

	public ParadigmProfileWithSettings(
		Class<? extends ParallelizationParadigm> paradigmType, String profileName)
	{
		super(paradigmType, profileName);
	}

	protected Class<T> getTypeOfSettings() {
		if (settings != null) {
			@SuppressWarnings("unchecked")
			Class<T> result = (Class<T>) settings.getClass();
			return result;
		}

		return null;
	}
}
