
package cz.it4i.parallel.internal.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.BooleanUtils;
import org.scijava.InstantiableException;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.PluginInfo;

import cz.it4i.parallel.internal.ParadigmManagerService;
import cz.it4i.parallel.paradigm_managers.ParadigmProfileUsingRunner;
import cz.it4i.parallel.paradigm_managers.ui.HavingOwnerWindow;
import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParadigmScreenController extends Pane implements CloseableControl {

	@FXML
	private ComboBox<Class<? extends ParallelizationParadigm>> paradigms;

	@FXML
	private ComboBox<ParallelizationParadigmProfile> cmbProfiles;

	@FXML
	private ComboBox<ParadigmManager> cmbParadigmManagers;

	@FXML
	private CheckBox chkActive;

	@FXML
	private Button btnCreate;

	@FXML
	private Button btnDelete;

	@FXML
	private TextField txtNameOfNewProfile;

	@FXML
	private TextField txtProfileType;

	@FXML
	private TextField txtProfileManager;

	@FXML
	private TextField txtActiveProfile;

	@FXML
	private TextField txtActiveProfileType;

	@FXML
	private TextField txtActiveProfileManager;

	@FXML
	private CheckBox chkRunning;

	@FXML
	private Button okButton;

	private ParallelService parallelService;

	private ParadigmManagerService paradigmManagerService;

	private ParallelizationParadigmProfile activeProfile;

	private Map<String, Boolean> userCheckedProfiles;

	public ParadigmScreenController() {
		JavaFXRoutines.initRootAndController("paradigm-screen.fxml", this);
		txtNameOfNewProfile.textProperty().addListener((a, b,
			c) -> updateCreateNewProfileButton());

		userCheckedProfiles = new HashMap<>();
	}

	public void activateDeactivate() {
		performOperationWithServer(this::activateDeactivateOperation);
	}

	public void initWithServices(ParallelService service,
		ParadigmManagerService initParadigmManagerService)
	{
		this.parallelService = service;
		this.paradigmManagerService = initParadigmManagerService;
		initParadigms();
		initProfiles();
		initActiveProfile();
		updateCreateNewProfileButton();
	}

	@Override
	public void close() {
		Stage stage = (Stage) okButton.getScene().getWindow();
		stage.close();
	}

	public void createNewProfile() {
		ParadigmManager manager = cmbParadigmManagers.getItems().isEmpty() ? null
			: cmbParadigmManagers.getSelectionModel().getSelectedItem();
		ParallelizationParadigmProfile profile;

		// If paradigm has not been configured it should not be created:
		boolean paradigmIsCorrect = false;
		// If it already exists it should not be created again.
		boolean exists = false;
		
		String newProfileName = txtNameOfNewProfile.getText();
		if (manager != null) {
			profile = manager.createProfile(newProfileName);
		}
		else {
			profile = new ParallelizationParadigmProfile(paradigms.getValue(),
				newProfileName);
		}

		try {
			parallelService.addProfile(profile);
			if (manager != null) {
				paradigmIsCorrect = manager.editProfile(profile);
			}
			else {
				paradigmIsCorrect = true;
			}

			if (paradigmIsCorrect) {
				cmbProfiles.getItems().add(profile);
				cmbProfiles.getSelectionModel().select(profile);
				txtNameOfNewProfile.setText("");
			}
		}
		catch (IllegalArgumentException exc) {
			exists = true;
			SimpleDialog.showError("There is already a profile with the same name!",
				exc.getMessage());
		}
		if (!paradigmIsCorrect && !exists) {
			parallelService.deleteProfile(profile.toString());
		}
	}

	public void deleteProfile() {
		if (!cmbProfiles.getSelectionModel().isEmpty()) {
			ParallelizationParadigmProfile toDelete = cmbProfiles.getSelectionModel()
				.getSelectedItem();
			int indexToSelect = cmbProfiles.getSelectionModel().getSelectedIndex();
			parallelService.deleteProfile(toDelete.toString());
			cmbProfiles.getItems().remove(toDelete);
			indexToSelect = Math.min(indexToSelect, cmbProfiles.getItems().size() -
				1);
			if (indexToSelect >= 0) {
				cmbProfiles.getSelectionModel().select(indexToSelect);

				// If the selected profile is deleted, select the last one, if
				// any are available:
				if (toDelete == activeProfile) {
					parallelService.selectProfile(cmbProfiles.getSelectionModel()
						.getSelectedItem().toString());
					updateActiveProfile();
				}
			}
			if (BooleanUtils.isTrue(toDelete.isSelected())) {
				updateActiveProfile();
			}
		}
	}

	public void editProfile() {
		if (!cmbProfiles.getSelectionModel().isEmpty()) {
			ParallelizationParadigmProfile profile = cmbProfiles.getSelectionModel()
				.getSelectedItem();
			runEditProfile(profile);
		}
	}

	@FXML
	public void runEnd() {
		performOperationWithServer(this::runEndOperation);
	}

	public void paradigmSelected() {
		cmbParadigmManagers.getItems().clear();
		cmbParadigmManagers.getItems().addAll(paradigmManagerService.getManagers(
			paradigms.getValue()));
		if (cmbParadigmManagers.getItems().isEmpty()) {
			cmbParadigmManagers.setDisable(true);
		}
		else {
			cmbParadigmManagers.setDisable(false);
			cmbParadigmManagers.getSelectionModel().select(0);
		}
	}

	public void profileSelected() {
		if (!cmbProfiles.getSelectionModel().isEmpty()) {
			ParallelizationParadigmProfile profile = cmbProfiles.getSelectionModel()
				.getSelectedItem();

			txtProfileType.setText(profile.getParadigmType().getSimpleName());

			setProfileManagerName(profile, txtProfileManager);

			btnDelete.setDisable(false);
		}
		else {
			txtProfileType.setText("");
			txtProfileManager.setText("");
			btnDelete.setDisable(true);
		}
	}

	private void setProfileManagerName(ParallelizationParadigmProfile profile,
		TextField textField)
	{
		try {
			textField.setText(paradigmManagerService.getManagers(profile).toString());
			textField.setDisable(false);
		}
		catch (Exception exc) {
			textField.setText("");
			textField.setDisable(true);
		}

	}

	public void selectProfile() {
		if (!cmbProfiles.getSelectionModel().isEmpty()) {
			parallelService.selectProfile(cmbProfiles.getSelectionModel()
				.getSelectedItem().toString());
			updateActiveProfile();
		}
	}

	private void activateDeactivateOperation() {
		if (chkActive.isSelected()) {
			initProfile();
		}
		else {
			closeProfile();
		}
	}

	private void runEndOperation() {
		if (!chkRunning.isSelected()) {
			if (activeProfile instanceof ParadigmProfileUsingRunner<?>) {
				ParadigmProfileUsingRunner<?> typedProfile =
					(ParadigmProfileUsingRunner<?>) activeProfile;
				typedProfile.setShutdownOnParadigmClose();
			}
			if (!chkActive.isSelected()) {
				ParallelizationParadigm paradigm = parallelService.getParadigmOfType(
					ParallelizationParadigm.class);
				paradigm.init();
			}
		}
		chkActive.setSelected(chkRunning.isSelected());
		activateDeactivateOperation();
	}

	private void closeProfile() {
		try (ParallelizationParadigm paradigm = parallelService.getParadigmOfType(
			ParallelizationParadigm.class))
		{
			// only close
		}
	}

	private ParadigmManager findManager(ParallelizationParadigmProfile profile) {
		ParadigmManager result = paradigmManagerService.getManagers(profile);
		if (result instanceof HavingOwnerWindow<?>) {
			HavingOwnerWindow<?> havingParent = (HavingOwnerWindow<?>) result;
			if (havingParent.getType().isInstance(getOwnerWindow())) {
				@SuppressWarnings("unchecked")
				HavingOwnerWindow<Window> typed =
					(HavingOwnerWindow<Window>) havingParent;
				typed.setOwner(getOwnerWindow());
			}
		}

		return result;
	}

	private Window getOwnerWindow() {
		return getScene().getWindow();
	}

	private void haveUserCheckSettings() {
		if (!(activeProfile instanceof ParadigmProfileUsingRunner)) {
			return;
		}
		if (chkActive.isSelected()) {
			ParadigmProfileUsingRunner<?> profile =
				(ParadigmProfileUsingRunner<?>) activeProfile;
			String name = activeProfile.toString();
			if (Boolean.FALSE.equals(userCheckedProfiles.computeIfAbsent(name,
				x -> false)))
			{
				runEditProfile(profile);
				userCheckedProfiles.put(name, true);
			}
		}
	}

	private void initActiveProfile() {
		JavaFXRoutines.runOnFxThread(this::updateActiveProfile);

	}

	private void initParadigms() {
		List<PluginInfo<ParallelizationParadigm>> parallelPlugins = parallelService
			.getPlugins();
		log.debug("Number of parallel plugins: {} ", parallelPlugins.size());
		for (PluginInfo<ParallelizationParadigm> info : parallelPlugins) {
			try {
				info.loadClass();
				paradigms.getItems().add(info.getPluginClass());
				log.debug("Parallel plugin: {} ", info.getPluginClass());

				paradigms.setConverter(
					new StringConverter<Class<? extends ParallelizationParadigm>>()
					{

						@Override
						public String toString(
							Class<? extends ParallelizationParadigm> item)
					{
							return (item == null) ? "" : item.getSimpleName();
						}

						@Override
						public Class<? extends ParallelizationParadigm> fromString(
							String s)
					{
							throw new UnsupportedOperationException();
						}
					});
			}
			catch (InstantiableException exc) {
				log.error("Faild to load parallel plugin class.");
			}
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!paradigms.getItems().isEmpty()) {
				paradigms.getSelectionModel().selectFirst();

				paradigmSelected();
			}
		});
	}

	private void initProfile() {
		ParallelizationParadigm paradigm = parallelService.getParadigmOfType(
			ParallelizationParadigm.class);
		if (paradigm != null) {
			paradigm.init();
			parallelService.saveProfiles();
		}
	}

	private void initProfiles() {
		for (ParallelizationParadigmProfile profile : parallelService
			.getProfiles())
		{
			cmbProfiles.getItems().add(profile);
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!cmbProfiles.getItems().isEmpty()) {
				cmbProfiles.getSelectionModel().selectFirst();

				txtProfileType.setText(cmbProfiles.getSelectionModel().getSelectedItem()
					.getParadigmType().getSimpleName());
				setProfileManagerName(cmbProfiles.getSelectionModel().getSelectedItem(),
					txtProfileManager);
			}
		});
	}

	private void performOperationWithServer(Runnable run) {
		this.setDisable(true);
		haveUserCheckSettings();
		CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
			try {
				run.run();
			}
			finally {
				JavaFXRoutines.runOnFxThread(() -> {
					this.setDisable(false);
					this.updateActiveProfile();
				});
			}
		});

		cf.exceptionally(t -> {
			chkActive.setSelected(false);
			userCheckedProfiles.put(activeProfile.toString(), false);
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showException(
				"Connection error!", "See the exception bellow for more details.",
				new Exception(t)));
			log.info(t.getMessage(), t);
			return null;
		});
	}

	private void runEditProfile(ParallelizationParadigmProfile profile) {
		ParadigmManager manager = findManager(profile);
		if (manager != null) {
			manager.editProfile(profile);
		}
		parallelService.saveProfiles();
	}

	private void updateCreateNewProfileButton() {
		btnCreate.setDisable(txtNameOfNewProfile.getText().trim().isEmpty());
	}

	private void updateActiveProfile() {
		Optional<ParallelizationParadigmProfile> profile = parallelService
			.getProfiles().stream().filter(p -> p.isSelected() != null && p
				.isSelected()).findAny();
		if (profile.isPresent()) {
			this.activeProfile = profile.get();
			txtActiveProfile.setText(profile.get().toString());
			txtActiveProfileType.setText(profile.get().getParadigmType()
				.getSimpleName());
			boolean paradigmActive = parallelService.getParadigmOfType(
				ParallelizationParadigm.class).getStatus() == Status.ACTIVE;
			chkActive.setSelected(paradigmActive);
			chkActive.setDisable(false);
			if (profile.get() instanceof ParadigmProfileUsingRunner) {
				ParadigmProfileUsingRunner<?> typedProfile =
					(ParadigmProfileUsingRunner<?>) profile.get();
				chkRunning.setVisible(true);
				chkRunning.setSelected(typedProfile.getAssociatedRunner()
					.getStatus() == Status.ACTIVE);
			}
			else {
				chkRunning.setVisible(false);
			}

			setProfileManagerName(profile.get(), txtActiveProfileManager);
		}
		else {
			activeProfile = null;
			txtActiveProfile.setText("");
			txtActiveProfileType.setText("");
			txtActiveProfileManager.setText("");
			chkActive.setDisable(true);
		}
	}
}
