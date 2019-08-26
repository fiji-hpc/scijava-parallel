/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/

package org.scijava.parallel;

import java.io.Closeable;

import org.scijava.plugin.SingletonPlugin;

/**
 * Objects of this type are used for parallelization in scijava.
 * 
 * @author Petr Bainar, Jan Kožusznik
 */

public interface ParallelizationParadigm extends SingletonPlugin, Closeable {

	void init();

	Status getStatus();

	// -- Closeable methods --
	@Override
	default void close() {

	}

}
