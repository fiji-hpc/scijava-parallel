
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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LocalSettingsScreenController {

	@FXML
	private TextField localDirectoryTextField;

	@FXML
	private TextField commandTextField;

	@FXML
	public Button okButton;
	
	private String command = "ImageJ-linux64";

	private File localDirectory;

	private ImageJServerRunnerSettings settings;

	public void fillInputs(final ImageJServerRunnerSettings settings,
		final Map<String, Object> inputs)
	{
		final Path fiji = Paths.get(settings.getFijiExecutable());
		inputs.put("localDirectory", fiji.getParent().toString());
		inputs.put("command", fiji.getFileName().toString());
	}

	public void run(final Map<String, Object> inputs) {
		// Reload settings from input if they already exist:
		if (!inputs.isEmpty()) {
			this.localDirectory = new File((String) inputs.get("localDirectory"));
			this.command = (String) inputs.get("command");
		}
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
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Open Resource File");
		Stage stage = new Stage();
		File selectedDirectory = directoryChooser.showDialog(stage);
		if(selectedDirectory != null) {
			this.localDirectory = selectedDirectory;
			this.localDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
		}
	}
	
	@FXML
	private void okAction() {
		// Set settings:
		this.localDirectory = new File(localDirectoryTextField.getText());
		this.command = commandTextField.getText();
		
		// Save the settings:
		final Path fiji = localDirectory.toPath().resolve(command);
		this.settings = ImageJServerRunnerSettings.builder().fiji(fiji.toString())
			.build();
		
		// Close the modal window:
		Stage stage = (Stage) okButton.getScene().getWindow();
		stage.close();
	}
	
	private void openWindow() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
			"local-settings-screen.fxml"));
		try {
			Parent fxmlFile = fxmlLoader.load();
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.setTitle("Local ImageJ Server Settings");
			stage.setScene(new Scene(fxmlFile));
			stage.show();
		}
		catch (IOException exc) {
			System.out.println(exc);
		}
	}

}
