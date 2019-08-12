package org.scijava.parallel;


public interface HavingParentWindows<T> {

	Class<T> getType();

	void initParent(T parent);
}
