
package cz.it4i.parallel.ui;

import org.scijava.prefs.PrefService;

import cz.it4i.parallel.runners.HPCSettings;
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
			prefService,
			"hpcSettingsForm", this.getClass());
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
		if(this.prefService == null) {
			this.prefService = newPrefService;
		}
	}
}
