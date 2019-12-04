/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel.internal;

import cz.it4i.common.ExceptionRoutines;
import cz.it4i.common.ExceptionRoutines.RunnableWithException;
import cz.it4i.common.ExceptionRoutines.SupplierWithException;
import cz.it4i.parallel.SciJavaParallelRuntimeException;

public final class InternalExceptionRoutines {

	private InternalExceptionRoutines() {}

	public static void rethrowAsUnchecked(Throwable exc) {
		ExceptionRoutines.rethrowAsUnchecked(exc,
			SciJavaParallelRuntimeException::new);
	}

	public static void runWithExceptionHandling(RunnableWithException runnable)
	{
		ExceptionRoutines.runWithExceptionHandling(runnable,
			SciJavaParallelRuntimeException::new);
	}

	public static <T> T supplyWithExceptionHandling(
		SupplierWithException<T> supplier)
	{
		return ExceptionRoutines.supplyWithExceptionHandling(supplier,
			SciJavaParallelRuntimeException::new);
	}
}
