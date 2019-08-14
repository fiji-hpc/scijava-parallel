
package cz.it4i.parallel.ui;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.parallel.runners.RunnerSettingsEditor;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class HPCSettingsScreenWindow {

	private HPCSettingsScreenController controller;

	private Window owner;

	private PrefService prefService;

	public HPCSettings showDialog(final HPCSettings oldSettings) {
		HPCSettings settings;

		// Get the old settings:
		settings = oldSettings;

		// if the old settings are null set the last time's
		// settings of this form.
		LastFormLoader<HPCSettings> storeLastForm = new LastFormLoader<>(
			prefService, "hpcSettingsForm", this.getClass());
		if (settings == null) {
			settings = storeLastForm.loadLastForm();
		}

		// Create controller:
		this.controller = new HPCSettingsScreenController();
		// Initialize form values with default or old settings:
		this.controller.setInitialFormValues(settings);
		// Request new settings:
		this.openWindow();

		// If the user did not provide new settings:
		if (this.controller.getSettings() != null) {
			// Return old settings.
			settings = this.controller.getSettings();
		}

		storeLastForm.storeLastForm(settings);

		// Return the new settings.
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
		parentStage.setTitle("HPC Settings");
		parentStage.setScene(formScene);
		parentStage.initOwner(owner);

		parentStage.showAndWait();
	}

	public void initialize(PrefService newPrefService) {
		if (this.prefService == null) {
			this.prefService = newPrefService;
		}
	}

	@Plugin(type = RunnerSettingsEditor.class, priority = Priority.HIGH)
	public static class Editor implements RunnerSettingsEditor<HPCSettings> {

		@Parameter
		private Context context;

		@Override
		public Class<HPCSettings> getTypeOfSettings() {
			return HPCSettings.class;
		}

		@Override
		public HPCSettings edit(HPCSettings settings) {
			HPCSettingsScreenWindow hpcSettingsScreenWindow =
				new HPCSettingsScreenWindow();
			hpcSettingsScreenWindow.initialize(context.getService(
				PrefService.class));
			return hpcSettingsScreenWindow.showDialog(settings);
		}
	}
}
