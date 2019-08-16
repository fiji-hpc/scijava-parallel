package cz.it4i.parallel;


public class SciJavaParallelRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -2924025307487207524L;

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
