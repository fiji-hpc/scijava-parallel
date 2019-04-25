
package cz.it4i.parallel;

import java.nio.file.Files;
import java.nio.file.Path;

public class Routines {

	public interface RunnableWithException {

		public void run() throws Exception;
	}

	public interface SupplierWithException<T> {

		public T supply() throws Exception;
	}

	public static void runWithExceptionHandling(RunnableWithException runnable)
	{
		try {
			runnable.run();
		}
		catch (Exception exc) {
			throw new SciJavaParallelRuntimeException(unwrapException(exc));
		}
	}

	public static void runWithExceptionSuppress(RunnableWithException run) {
		try {
			run.run();
		}
		catch (Exception e) {
			// ignore this
		}
	}

	public static <T> T supplyWithExceptionHandling(
		SupplierWithException<T> supplier)
	{
		try {
			return supplier.supply();
		}
		catch (Exception exc) {
			throw new SciJavaParallelRuntimeException(unwrapException(exc));
		}
	}

	public static String getSuffix(String filename) {
		return filename.substring(filename.lastIndexOf('.'), filename.length());
	}

	public static <T> T castTo(Object src) {
		@SuppressWarnings("unchecked")
		T result = (T) src;
		return result;
	}

	public static Path getTempFileForSuffix(String workingSuffix) {
		return Routines.supplyWithExceptionHandling(() -> Files.createTempFile(
			Thread.currentThread().toString(), workingSuffix));
	}

	public static Throwable unwrapException(Throwable exc) {
		if (exc.getCause() == null) {
			return exc;
		}
		return unwrapException(exc.getCause());
	}
}
