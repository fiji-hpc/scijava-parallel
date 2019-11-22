package cz.it4i.parallel.paradigm_managers;

import java.io.Serializable;
import java.util.function.Function;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;


public abstract class ParadigmManagerWithSettings<S extends Serializable>
	implements ParadigmManager
{

	@SuppressWarnings("unchecked")
	protected static <C, R> R runWithResultForObjectIfOfTypeElseException(
		Object profile, Class<C> type, Function<C, R> predicate)
	{
		if (type.isInstance(profile)) {
			return predicate.apply((C) profile);
		}
		throw new UnsupportedOperationException("Not supported for profile: " +
			profile);
	}

	@Parameter
	private PluginService pluginService;

	@Override
	public boolean editProfile(ParallelizationParadigmProfile profile) {
		return runWithResultForObjectIfOfTypeElseException(profile,
			ParadigmProfileWithSettings.class, this::editSettings);
	}

	protected boolean editSettings(
		ParadigmProfileWithSettings<S> typedProfile)
	{
		ParadigmProfileSettingsEditor<S> editor = getEditor(typedProfile
			.getTypeOfSettings());
		if (editor == null) {
			return false;
		}
		typedProfile.setSettings(editor.edit(typedProfile.getSettings()));
		return typedProfile.getSettings() != null;
	}

	private ParadigmProfileSettingsEditor<S> getEditor(
		Class<S> clazz)
	{
		@SuppressWarnings("unchecked")
		ParadigmProfileSettingsEditor<S> result = pluginService.createInstancesOfType(
			ParadigmProfileSettingsEditor.class).stream().filter(rse -> rse.getTypeOfSettings()
				.equals(clazz)).findFirst().orElse(null);
		return result;
	}
}
