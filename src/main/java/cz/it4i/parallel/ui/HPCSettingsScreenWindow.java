
package cz.it4i.parallel.ui;



import cz.it4i.parallel.runners.HPCSettings;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class HPCSettingsScreenWindow 
{

	private HPCSettingsScreenController controller;

	private HPCSettings settings;

	private Window owner;

	public HPCSettings showDialog(final HPCSettings oldSettings) {
		// Get the old settings:

		if (oldSettings != null) {
			this.settings = oldSettings;
		}
		this.controller = new HPCSettingsScreenController();
		setInitialTextFieldText();
		// Request new settings:
		this.openWindow();

		// If the user did not provide new settings:
		if (controller.getSettings() == null) {
			// Return old settings.
			return this.settings;
		}
		// Return the new settings.
		return controller.getSettings();
	}

	private void openWindow() {
		final Scene fileSelectionScene = new Scene(controller);
		final Stage parentStage = new Stage();
		parentStage.initModality(Modality.APPLICATION_MODAL);
		parentStage.setResizable(false);
		parentStage.setTitle("HPC Settings");
		parentStage.setScene(fileSelectionScene);
		parentStage.initOwner(owner);
		// Set the text fields to the old settings:

		parentStage.showAndWait();
	}

	private void setInitialTextFieldText() {
		if (settings != null) {
			controller.hostTextField.setText(settings.getHost());
			controller.portSpinner.getValueFactory().setValue(settings.getPort());
			controller.userNameTextField.setText(settings.getUserName());
			// Get authentication choice:
			if (settings.getAuthenticationChoice().equals("Key file")) {
				controller.authenticationChoiceKeyRadioButton.setSelected(true);
				controller.authenticationChoicePasswordRadioButton.setSelected(false);
				controller.disableIrrelevantFileds(true);
			}
			else {
				controller.authenticationChoiceKeyRadioButton.setSelected(false);
				controller.authenticationChoicePasswordRadioButton.setSelected(true);
				controller.disableIrrelevantFileds(false);
			}
			controller.keyFileTextField.setText(settings.getKeyFile()
				.getAbsolutePath());
			controller.keyFilePasswordPasswordField.setText(settings
				.getKeyFilePassword());
			controller.passwordPasswordField.setText(settings.getPassword());
			controller.schedulerTypeComboBox.getSelectionModel().select(settings
				.getAdapterType().toString());
			controller.remoteDirectoryTextField.setText(settings
				.getRemoteDirectory());
			controller.commandTextField.setText(settings.getCommand());
			controller.nodesSpinner.getValueFactory().setValue(settings.getNodes());
			controller.ncpusSpinner.getValueFactory().setValue(settings.getNcpus());
			controller.shutdownJobAfterCloseCheckBox.setSelected(settings
				.isShutdownOnClose());
			controller.redirectStdOutErrCheckBox.setSelected(settings
				.isRedirectStdInErr());
		}
	}

	public void setOwner(Window aOwner) {
		this.owner = aOwner;
	}
}
