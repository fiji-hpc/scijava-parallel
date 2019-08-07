
package cz.it4i.parallel.ui;

import java.io.File;
import java.nio.file.Path;

import cz.it4i.parallel.runners.ImageJServerRunnerSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class LocalSettingsScreenController {

	@FXML
	public TextField localFijiExecutablePathTextField;

	@FXML
	public Button okButton;
	
	@FXML
	public Button browseButton;
	
	@Getter
	@Setter
	private File localFijiExecutablePath;
	
	@Getter
	@Setter
	private ImageJServerRunnerSettings settings;

	private ImageJServerRunnerSettings createSettings() {
		Path fijiPath = localFijiExecutablePath.toPath();
		return ImageJServerRunnerSettings.builder().fiji(fijiPath.toString()).build();
	}

	@FXML
	private void browseAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Fiji Executable File");
		Stage stage = (Stage) browseButton.getScene().getWindow();
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {			
			this.localFijiExecutablePathTextField.setText(selectedFile
				.getAbsolutePath());
		}
	}

	@FXML
	private void okAction() {
		// Check if the selected Fiji executable exists:
		this.localFijiExecutablePath = new File(this.localFijiExecutablePathTextField.getText());
		if(!this.localFijiExecutablePath.exists() || this.localFijiExecutablePath.isDirectory()) {
			showErrorDialog("File selected does not exits!", "Select the fiji executable file!");
			return;
		}
		
		try{			
			// Save the settings:
			this.settings = createSettings();
			
			// Close the modal window:
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		} catch (Exception exc) {
			showErrorDialog("Exception", exc.toString());
		}
		
	}

	private void showErrorDialog(String header, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
