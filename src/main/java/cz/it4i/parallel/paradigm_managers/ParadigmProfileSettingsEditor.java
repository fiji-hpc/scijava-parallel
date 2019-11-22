package cz.it4i.parallel.paradigm_managers;

import org.scijava.plugin.SingletonPlugin;

public interface ParadigmProfileSettingsEditor<S> extends
	SingletonPlugin
{

	Class<S> getTypeOfSettings();

	S edit(S settings);

}
