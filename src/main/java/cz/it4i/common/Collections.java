package cz.it4i.common;

import java.util.Collection;

public final class Collections {

	private Collections() {}

	public static <T extends AutoCloseable> AutoCloseable closeBoth(T a, T b) {
	  if(a==null) return b;
	  if(b==null) return a;
	  return () -> { try(AutoCloseable first=a) { b.close(); } };
	}


	public static AutoCloseable closeAll(
		Collection<? extends AutoCloseable> coll)
	{
		@SuppressWarnings("unchecked")
		Collection<AutoCloseable> casted = (Collection<AutoCloseable>) coll;
		return casted.stream().reduce(null, Collections::closeBoth);
	}

	

}
