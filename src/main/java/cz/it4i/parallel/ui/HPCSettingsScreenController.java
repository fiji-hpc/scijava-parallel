
package cz.it4i.parallel.ui;

import java.io.File;

import cz.it4i.parallel.runners.HPCSchedulerType;
import cz.it4i.parallel.runners.HPCSettings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;

public class HPCSettingsScreenController {

	@FXML
	private TextField hostTextField;

	@FXML
	private Spinner<Integer> portSpinner;

	@FXML
	private TextField userNameTextField;

	@FXML
	private ToggleGroup authenticationMethod;

	@FXML
	private RadioButton authenticationChoiceKeyRadioButton;

	@FXML
	private RadioButton authenticationChoicePasswordRadioButton;

	@FXML
	private TextField keyFileTextField;

	@FXML
	private PasswordField keyFilePasswordPasswordField;

	@FXML
	private PasswordField passwordPasswordField;

	@FXML
	private ComboBox<String> schedulerTypeComboBox;

	@FXML
	private TextField remoteDirectoryTextField;

	@FXML
	private TextField commandTextField;

	@FXML
	private Spinner<Integer> nodesSpinner;

	@FXML
	private Spinner<Integer> ncpusSpinner;

	@FXML
	private CheckBox shutdownJobAfterCloseCheckBox;

	@FXML
	private CheckBox redirectStdOutErrCheckBox;

	private HPCSettings settings;

	private static final String KEY_FILE_OPTION = "KeyFile";
	
	private static final String PASSWORLD_OPTION = "Password";
	
	String host;
	int port;
	String authenticationChoice;
	String userName;
	String password;
	File keyFile;
	String keyFilePassword;
	String remoteDirectory;
	String command;
	int nodes;
	int ncpus;
	boolean shutdownJobAfterClose;
	boolean redirectStdOutErr;
	String schedulerType;

	@FXML
	public void initialize() {
		schedulerTypeComboBox.getItems().removeAll(schedulerTypeComboBox.getItems());
		schedulerTypeComboBox.getItems().addAll(KEY_FILE_OPTION, PASSWORLD_OPTION);
		schedulerTypeComboBox.getSelectionModel().select(KEY_FILE_OPTION);
	}
	
	@FXML
	private void browseAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Fiji Executable File");
		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {			
			this.keyFilePasswordPasswordField.setText(selectedFile
				.getAbsolutePath());
		}
	}
	
	private void okAction() {
		host = hostTextField.getText();
		port = portSpinner.getValue();
		// authenticationChoice
		if (authenticationChoiceKeyRadioButton.isSelected()) {
			authenticationChoice = KEY_FILE_OPTION;
		}
		else {
			authenticationChoice = PASSWORLD_OPTION;
		}
		userName = userNameTextField.getText();
		password = passwordPasswordField.getText();
		keyFile = new File(keyFilePasswordPasswordField.getText());
		remoteDirectory = remoteDirectoryTextField.getText();
		command = commandTextField.getText();
		nodes = nodesSpinner.getValue();
		ncpus = ncpusSpinner.getValue();
		shutdownJobAfterClose = shutdownJobAfterCloseCheckBox.isSelected();
		redirectStdOutErr = redirectStdOutErrCheckBox.isSelected();
		schedulerType = schedulerTypeComboBox.getSelectionModel().getSelectedItem();
	}

	private HPCSettings createSettings() {
		return HPCSettings.builder().host(host).portNumber(port).userName(userName)
			.authenticationChoice(authenticationChoice).password(password).keyFile(
				keyFile).keyFilePassword(keyFilePassword).remoteDirectory(
					remoteDirectory).command(command).nodes(nodes).ncpus(ncpus)
			.shutdownOnClose(shutdownJobAfterClose).redirectStdInErr(
				redirectStdOutErr).adapterType(HPCSchedulerType.getByString(
					schedulerType)).build();
	}

}
