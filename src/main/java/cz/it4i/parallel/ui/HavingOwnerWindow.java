package cz.it4i.parallel.ui;


public interface HavingOwnerWindow<T> {

	Class<T> getType();

	void setOwner(T parent);
}
