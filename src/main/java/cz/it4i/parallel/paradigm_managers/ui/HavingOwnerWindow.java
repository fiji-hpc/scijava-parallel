package cz.it4i.parallel.paradigm_managers.ui;


public interface HavingOwnerWindow<T> {

	Class<T> getType();

	void setOwner(T parent);
}
