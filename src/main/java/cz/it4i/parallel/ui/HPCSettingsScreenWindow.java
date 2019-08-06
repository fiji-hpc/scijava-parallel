package cz.it4i.parallel.ui;

import java.io.IOException;

import cz.it4i.parallel.runners.HPCSettings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HPCSettingsScreenWindow {
	
	private HPCSettingsScreenController controller;
	
	private HPCSettings settings;
	
	private void openWindow() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
			"hpc-settings-screen.fxml"));
		try {
			Parent fxmlFile = fxmlLoader.load();
			this.controller = fxmlLoader.getController();
			Scene fileSelectionScene = new Scene(fxmlFile);
			Stage parentStage = new Stage();
			parentStage.initModality(Modality.APPLICATION_MODAL);
			parentStage.setResizable(false);
			parentStage.setTitle("HPC Settings");
			parentStage.setScene(fileSelectionScene);

			// Set the text fields to the old settings:
			setInitialTextFieldText();

			parentStage.showAndWait();
		}
		catch (IOException exc) {
			showErrorDialog(exc.toString(), "FXML file is missing!");
		}
	}
	
	public HPCSettings showDialog(
		final HPCSettings oldSettings)
	{
		// Get the old settings:
		if (oldSettings != null) {
			this.settings = oldSettings;
		}

		// Request new settings:
		this.openWindow();
		
		// If the user did not provide new settings:
		if(controller.getSettings() == null)
		{
			// Return old settings.
			return this.settings;
		}
		// Return the new settings.
		return controller.getSettings();		
	}
	
	private void setInitialTextFieldText() {
//		if (settings == null) {			
//		}
//		else {
//		}
	}
	
	private void showErrorDialog(String header, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
