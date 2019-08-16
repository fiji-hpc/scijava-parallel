
package cz.it4i.parallel.ui;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SimpleDialog {
	
	private SimpleDialog(){
		
	}

	public static void showError(String header, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(message);

		alert.showAndWait();
	}
	
	public static File fileChooser(Stage stage, String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			return selectedFile;
		}
		return null;
	}
}
