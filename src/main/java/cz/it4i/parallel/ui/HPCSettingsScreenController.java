
package cz.it4i.parallel.ui;

import java.awt.Window;
import java.io.File;

import cz.it4i.parallel.runners.HPCSchedulerType;
import cz.it4i.parallel.runners.HPCSettings;
import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.FXFrame;
import cz.it4i.swing_javafx_ui.InitiableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.Setter;

public class HPCSettingsScreenController extends AnchorPane implements
	CloseableControl, InitiableControl
{

	@FXML
	public TextField hostTextField;

	@FXML
	public Spinner<Integer> portSpinner;

	@FXML
	public TextField userNameTextField;

	@FXML
	public ToggleGroup authenticationMethod;

	@FXML
	public RadioButton authenticationChoiceKeyRadioButton;

	@FXML
	public RadioButton authenticationChoicePasswordRadioButton;

	@FXML
	public TextField keyFileTextField;

	@FXML
	public PasswordField keyFilePasswordPasswordField;

	@FXML
	public PasswordField passwordPasswordField;

	@FXML
	public ComboBox<String> schedulerTypeComboBox;

	@FXML
	public TextField remoteDirectoryTextField;

	@FXML
	public TextField commandTextField;

	@FXML
	public Spinner<Integer> nodesSpinner;

	@FXML
	public Spinner<Integer> ncpusSpinner;

	@FXML
	public CheckBox shutdownJobAfterCloseCheckBox;

	@FXML
	public CheckBox redirectStdOutErrCheckBox;

	@FXML
	private Button okButton;

	@FXML
	private Button browseButton;

	@Getter
	@Setter
	private HPCSettings settings;

	private static final String PBS_OPTION = "PBS";

	private static final String SLURM_OPTION = "Slurm";

	private static final Integer SPINER_INITIAL_VALUE = 1;

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

	private Window parent;

	public HPCSettingsScreenController() {
		JavaFXRoutines.initRootAndController("hpc-settings-screen.fxml", this);
	}

	@Override
	public void init(Window parameter) {
		this.parent = parameter;
	}

	@FXML
	public void initialize() {
		// RadioButtons:
		authenticationChoiceKeyRadioButton.setSelected(true);

		// ComboBoxes:
		schedulerTypeComboBox.getItems().removeAll(schedulerTypeComboBox
			.getItems());
		schedulerTypeComboBox.getItems().addAll(PBS_OPTION, SLURM_OPTION);
		schedulerTypeComboBox.getSelectionModel().select(PBS_OPTION);

		// Spinners:
		SpinnerValueFactory<Integer> portValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535,
				SPINER_INITIAL_VALUE);
		portSpinner.setValueFactory(portValueFactory);

		SpinnerValueFactory<Integer> nodesValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,
				SPINER_INITIAL_VALUE);
		nodesSpinner.setValueFactory(nodesValueFactory);

		SpinnerValueFactory<Integer> ncpusValueFactory =
			new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,
				SPINER_INITIAL_VALUE);
		ncpusSpinner.setValueFactory(ncpusValueFactory);

		// Disable fields that are not relevant to authentication method selection:
		authenticationChoiceKeyRadioButton.selectedProperty().addListener((
			ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected,
			Boolean isNowSelected) -> disableIrrelevantFileds(isNowSelected));
	}

	public void disableIrrelevantFileds(Boolean isSelected) {
		if (isSelected) {
			passwordPasswordField.setDisable(true);
			keyFileTextField.setDisable(false);
			keyFilePasswordPasswordField.setDisable(false);
			browseButton.setDisable(false);
		}
		else {
			keyFileTextField.setDisable(true);
			keyFilePasswordPasswordField.setDisable(true);
			browseButton.setDisable(true);
			passwordPasswordField.setDisable(false);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	
	}

	@FXML
	private void browseAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Fiji Executable File");
		Stage stage = (Stage) browseButton.getScene().getWindow();
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			this.keyFileTextField.setText(selectedFile.getAbsolutePath());
		}
	}

	@FXML
	private void okAction() {
		host = hostTextField.getText();
		commitSpinnerValue(portSpinner);
		port = portSpinner.getValue();
		// authenticationChoice
		if (authenticationChoiceKeyRadioButton.isSelected()) {
			authenticationChoice = "Key file";
		}
		else {
			authenticationChoice = "Password";
		}
		userName = userNameTextField.getText();
		password = passwordPasswordField.getText();
		keyFile = new File(keyFileTextField.getText());
		keyFilePassword = keyFilePasswordPasswordField.getText();
		remoteDirectory = remoteDirectoryTextField.getText();
		command = commandTextField.getText();
		commitSpinnerValue(nodesSpinner);
		nodes = nodesSpinner.getValue();
		commitSpinnerValue(ncpusSpinner);
		ncpus = ncpusSpinner.getValue();
		shutdownJobAfterClose = shutdownJobAfterCloseCheckBox.isSelected();
		redirectStdOutErr = redirectStdOutErrCheckBox.isSelected();
		schedulerType = schedulerTypeComboBox.getSelectionModel().getSelectedItem();

		this.settings = createSettings();
		((FXFrame<?>) this.parent).dispose();
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

	private <T> void commitSpinnerValue(Spinner<T> spinner) {
		if (!spinner.isEditable()) return;
		String text = spinner.getEditor().getText();
		SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
		if (valueFactory != null) {
			StringConverter<T> converter = valueFactory.getConverter();
			if (converter != null) {
				T value = converter.fromString(text);
				valueFactory.setValue(value);
			}
		}
	}
}
