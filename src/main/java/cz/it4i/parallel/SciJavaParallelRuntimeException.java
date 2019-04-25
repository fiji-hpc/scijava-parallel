package cz.it4i.parallel;


public class SciJavaParallelRuntimeException extends RuntimeException {

	public SciJavaParallelRuntimeException() {
		super();
	}

	public SciJavaParallelRuntimeException(String message, Throwable cause,
		boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SciJavaParallelRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SciJavaParallelRuntimeException(String message) {
		super(message);
	}

	public SciJavaParallelRuntimeException(Throwable cause) {
		super(cause);
	}

}
