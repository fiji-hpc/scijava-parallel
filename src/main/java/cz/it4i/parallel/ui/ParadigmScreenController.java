package cz.it4i.parallel.ui;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.BooleanUtils;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.PluginInfo;

import cz.it4i.parallel.AbstractBaseRPCParadigmImpl;
import cz.it4i.parallel.ParadigmManagerService;
import cz.it4i.parallel.runners.ParadigmManagerUsingRunner;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.parallel.runners.RunnerSettings;
import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParadigmScreenController extends Pane implements CloseableControl
{

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
	private TextField txtActiveProfile;

	@FXML
	private TextField txtActiveProfileType;

	@FXML
	private CheckBox chkRunning;

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
		// do nothing
	}

	public void createNewProfile() {
		ParadigmManager manager = cmbParadigmManagers.getItems().isEmpty() ? null
			: cmbParadigmManagers.getSelectionModel().getSelectedItem();
		ParallelizationParadigmProfile profile;
		
		// If paradigm has not been configured it should not be created:
		boolean paradigmIsCorrect = false;
		
		if (manager != null) {
			profile = manager.createProfile(txtNameOfNewProfile.getText());
		}
		else {
			profile = new ParallelizationParadigmProfile(paradigms.getValue(),
				txtNameOfNewProfile.getText());
		}
		
		try {
			parallelService.addProfile(profile);
			if(manager != null) {
				paradigmIsCorrect = manager.editProfile(profile);
			}
			else {
				paradigmIsCorrect = true;
			}

			if (paradigmIsCorrect) {
				cmbProfiles.getItems().add(profile);
				cmbProfiles.getSelectionModel().select(profile);	
			}
		} catch (IllegalArgumentException exc) {
				SimpleDialog.showError("There is already a profile with the same name!", exc.getMessage());
		}
		if (!paradigmIsCorrect) {
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
			}
			if (BooleanUtils.isTrue(toDelete.isSelected())) {
				updateActiveProfile();
			}
		}
	}

	public void editProfile() {
		if (!cmbProfiles.getSelectionModel().isEmpty()) {
			ParallelizationParadigmProfile profile = cmbProfiles.getSelectionModel().getSelectedItem();
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
			txtProfileType.setText(cmbProfiles.getSelectionModel().getSelectedItem()
				.getParadigmType().getSimpleName());
			btnDelete.setDisable(false);
		}
		else {
			txtProfileType.setText("");
			btnDelete.setDisable(true);
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
			if (!userCheckedProfiles.computeIfAbsent(name, x -> false)) {
				runEditProfile(profile);
				userCheckedProfiles.put(name, true);
			}
		}
	}

	private void initActiveProfile() {
		JavaFXRoutines.runOnFxThread(this::updateActiveProfile);

	}

	private void initParadigms() {
		for (PluginInfo<ParallelizationParadigm> info : parallelService
			.getPlugins())
		{
			paradigms.getItems().add(info.getPluginClass());
			
			paradigms.setConverter(new StringConverter<Class<? extends ParallelizationParadigm>>() {
				@Override
				public String toString(Class<? extends ParallelizationParadigm> item) {
					return (item == null) ? "" : item.getSimpleName();
				}

				@Override
				public Class<? extends ParallelizationParadigm> fromString(String s) {
					throw new UnsupportedOperationException();
				}
			});
			
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!paradigms.getItems().isEmpty()) {
				paradigms.getSelectionModel().select(0);
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
				cmbProfiles.getSelectionModel().select(0);
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
			JavaFXRoutines.runOnFxThread(() -> SimpleDialog.showError(
				"Connection error!", t.getMessage()));
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
			activeProfile = profile.get();
			txtActiveProfile.setText(profile.get().toString());
			txtActiveProfileType.setText(profile.get().getParadigmType()
				.getSimpleName());
			boolean paradigmActive = parallelService.getParadigmOfType(
				ParallelizationParadigm.class)
				.getStatus() == Status.ACTIVE;
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
		}
		else {
			activeProfile = null;
			txtActiveProfile.setText("");
			txtActiveProfileType.setText("");
			chkActive.setDisable(true);
		}
	}
}
