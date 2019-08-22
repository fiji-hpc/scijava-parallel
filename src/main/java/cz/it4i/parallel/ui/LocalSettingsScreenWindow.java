
package cz.it4i.parallel.ui;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

import cz.it4i.parallel.runners.LocalImageJRunnerSettings;
import cz.it4i.parallel.runners.RunnerSettingsEditor;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LocalSettingsScreenWindow {

	@Plugin(type = RunnerSettingsEditor.class, priority = Priority.HIGH)
	public static class Editor implements
		RunnerSettingsEditor<LocalImageJRunnerSettings>, HavingOwnerWindow<Window>
	{

		@Parameter
		private Context context;
		private Window owner;

		@Override
		public Class<LocalImageJRunnerSettings> getTypeOfSettings() {
			return LocalImageJRunnerSettings.class;
		}

		@Override
		public LocalImageJRunnerSettings edit(
			LocalImageJRunnerSettings settings)
		{
			LocalSettingsScreenWindow localSettingsScreenWindow =
				new LocalSettingsScreenWindow();
			localSettingsScreenWindow.setOwner(owner);
			localSettingsScreenWindow.initialize(context.getService(
				PrefService.class));
			return localSettingsScreenWindow.showDialog(settings);
		}

		@Override
		public Class<Window> getType() {
			return Window.class;
		}

		@Override
		public void setOwner(Window parent) {
			this.owner = parent;
		}
	}

	private LocalSettingsScreenController controller;

	private Window owner;

	private PrefService prefService;

	public LocalImageJRunnerSettings showDialog(
		final LocalImageJRunnerSettings oldSettings)
	{
		LocalImageJRunnerSettings settings;

		// Get the old settings:
		settings = oldSettings;

		// if the old settings are null set the last time's
		// user approved settings of this form.
		LastFormLoader<LocalImageJRunnerSettings> storeLastForm =
			new LastFormLoader<>(prefService, "localSettingsForm", this.getClass());
		if (settings == null) {
			settings = storeLastForm.loadLastForm();
		}

		// Create controller:
		this.controller = new LocalSettingsScreenController();
		// Initialize form values with old settings, last approved or default :
		this.controller.setInitialFormValues(settings);
		// Request new settings from the user:
		this.openWindow();

		// If the user did provide new settings:
		if (this.controller.getSettings() != null) {
			// Set the new settings.
			settings = this.controller.getSettings();
			// Store the settings for this form.
			storeLastForm.storeLastForm(settings);
		}
		else {
			// The user has not accepted any settings and therefore they should be
			// empty or the old ones.
			if (oldSettings != null) {
				settings = oldSettings;
			}
			else {
				settings = null;
			}
		}

		// Return the settings.
		return settings;
	}

	public void setOwner(Window aOwner) {
		this.owner = aOwner;
	}

	private void openWindow() {
		final Scene formScene = new Scene(this.controller);
		final Stage parentStage = new Stage();
		parentStage.initModality(Modality.APPLICATION_MODAL);
		parentStage.setResizable(false);
		parentStage.setTitle("Local ImageJ Server Settings");
		parentStage.setScene(formScene);
		parentStage.initOwner(owner);

		parentStage.showAndWait();
	}

	public void initialize(PrefService newPrefService) {
		if (this.prefService == null) {
			this.prefService = newPrefService;
		}
	}
}
