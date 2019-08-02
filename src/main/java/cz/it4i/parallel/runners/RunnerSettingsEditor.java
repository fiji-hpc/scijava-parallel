package cz.it4i.parallel.runners;

import org.scijava.plugin.SingletonPlugin;

public interface RunnerSettingsEditor<S extends RunnerSettings> extends
	SingletonPlugin
{

	Class<S> getTypeOfSettings();

	S edit(S settings);

}
