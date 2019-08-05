
package cz.it4i.parallel.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LocalSettingsScreenController {

	@FXML
	private TextField localFijiExecutablePathTextField;

	@FXML
	public Button okButton;

	private File localFijiExecutablePath;

	private ImageJServerRunnerSettings settings;

	public void fillInputs(final ImageJServerRunnerSettings settings,
		final Map<String, Object> inputs)
	{
		final Path fiji = Paths.get(settings.getFijiExecutable());
		inputs.put("localDirectory", fiji.getParent().toString());
		inputs.put("command", fiji.getFileName().toString());
	}

	private ImageJServerRunnerSettings createSettings() {
		Path fiji = localFijiExecutablePath.toPath();
		settings = ImageJServerRunnerSettings.builder().fiji(fiji.toString())
			.build();
		return settings;
	}

	public void run(final Map<String, Object> inputs) {
//		// Reload settings from input if they already exist:
//		if (!inputs.isEmpty()) {
//			this.localDirectory = new File((String) inputs.get("localDirectory"));
//			this.command = (String) inputs.get("command");
//		}
	}

	public ImageJServerRunnerSettings showDialog(
		final Map<String, Object> inputs)
	{
		this.run(inputs);
		this.openWindow();
		return settings;
	}

	@FXML
	private void browseAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Fiji Executable File");
		Stage stage = new Stage();
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			this.localFijiExecutablePath = selectedFile;
			this.localFijiExecutablePathTextField.setText(selectedFile
				.getAbsolutePath());
		}
	}

	@FXML
	private void okAction() {
		// Save the settings:
		this.settings = createSettings();

		// Close the modal window:
		Stage stage = (Stage) okButton.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void initialize() {
		localFijiExecutablePathTextField.setText("");
	}

	private void openWindow() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
			"local-settings-screen.fxml"));
		try {
			Parent fxmlFile = fxmlLoader.load();
			fxmlLoader.getController();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.setTitle("Local ImageJ Server Settings");
			stage.setScene(new Scene(fxmlFile));
			stage.showAndWait();
		}
		catch (IOException exc) {
			showErrorDialog(exc.toString());
		}
	}
	
	private void showErrorDialog(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(message);
		alert.setContentText("Ooops, there was an error!");
		alert.showAndWait();
	}

	private void setInitialTextFieldText() {
		if (this.localFijiExecutablePath == null) {
			this.localFijiExecutablePathTextField.setText("");
		}
		else {
			this.localFijiExecutablePathTextField.setText(this.localFijiExecutablePath
				.getAbsolutePath());
		}
	}

}
