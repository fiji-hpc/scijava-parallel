package org.scijava.parallel;


public interface HavingOwnerWindow<T> {

	Class<T> getType();

	void setOwner(T parent);
}
