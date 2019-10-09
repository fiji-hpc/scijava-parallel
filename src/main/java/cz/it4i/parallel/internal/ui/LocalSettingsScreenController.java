
package cz.it4i.parallel.internal.ui;

import java.io.File;
import java.nio.file.Path;

import cz.it4i.parallel.paradigm_managers.LocalImageJRunnerSettings;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class LocalSettingsScreenController extends AnchorPane {

	@FXML
	private TextField localFijiExecutablePathTextField;

	@FXML
	private Button okButton;

	@FXML
	private Button browseButton;

	private File localFijiExecutablePath;

	@Getter
	@Setter
	private LocalImageJRunnerSettings settings;

	public LocalSettingsScreenController() {
		JavaFXRoutines.initRootAndController("local-settings-screen.fxml", this);
	}
	
	private LocalImageJRunnerSettings createSettings() {
		Path fijiPath = localFijiExecutablePath.toPath();
		return LocalImageJRunnerSettings.builder().fiji(fijiPath.toString())
			.build();
	}

	@FXML
	private void browseAction() {
		Stage stage = (Stage) browseButton.getScene().getWindow();
		File selectedFile = SimpleDialog.fileChooser(stage, "Open Fiji Executable File");
		if (selectedFile != null) {
			this.localFijiExecutablePathTextField.setText(selectedFile
				.getAbsolutePath());
		}
	}

	@FXML
	private void okAction() {
		// Check if the selected Fiji executable exists:
		this.localFijiExecutablePath = new File(
			this.localFijiExecutablePathTextField.getText());
		if (!this.localFijiExecutablePath.exists() || this.localFijiExecutablePath
			.isDirectory())
		{
			SimpleDialog.showError("File selected does not exits!",
				"Select the fiji executable file!");
			return;
		}

		try {
			// Save the settings:
			this.settings = createSettings();

			// Close the modal window:
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		}
		catch (Exception exc) {
			SimpleDialog.showError("Exception", exc.toString());
		}

	}

	public void setInitialFormValues(LocalImageJRunnerSettings oldSettings) {
		if (oldSettings == null) {
			localFijiExecutablePathTextField.setText("Empty");
		}
		else {
			localFijiExecutablePathTextField.setText(oldSettings.getFijiExecutable());
		}
	}
}
