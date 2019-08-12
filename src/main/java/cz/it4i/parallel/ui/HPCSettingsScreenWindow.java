
package cz.it4i.parallel.ui;

import java.awt.Window;

import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.swing_javafx_ui.FXFrame;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;

public class HPCSettingsScreenWindow extends
	FXFrame<HPCSettingsScreenController>
{

	public HPCSettingsScreenWindow(final Window parent,
		final HPCSettings oldSettings)
	{
		super(parent, HPCSettingsScreenController::new);
		controller = getFxPanel().getControl();
		JavaFXRoutines.runOnFxThread(() -> {
			setResizable(false);
			// Get the old settings:
			if (oldSettings != null) {
				this.settings = oldSettings;
			}
			setInitialTextFieldText();
		});
	}

	private transient HPCSettingsScreenController controller;

	private HPCSettings settings;

	public HPCSettings showDialog() {

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
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
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
}
