package cz.it4i.parallel.ui;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.BooleanUtils;
import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParadigmManagerService;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.parallel.Status;
import org.scijava.plugin.PluginInfo;

import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.scene.control.CheckBox;

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
	private TextField txtActiveProfile;

	@FXML
	private TextField txtActiveProfileType;

	private ParallelService parallelService;

	private ParadigmManagerService paradigmManagerService;

	private ParallelizationParadigmProfile activeProfile;

	public ParadigmScreenController() {
		JavaFXRoutines.initRootAndController("paradigm-screen.fxml", this);
		txtNameOfNewProfile.textProperty().addListener((a, b,
			c) -> updateCreateNewProfileButton());
	}

	public void activateDeactivate() {
		this.setDisable(true);
		CompletableFuture.runAsync(() -> {
			if (chkActive.isSelected()) {
				initProfile();
			}
			else {
				closeProfile();
			}
			JavaFXRoutines.runOnFxThread(() -> this.setDisable(false));
		});

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
		if (manager != null) {
			profile = manager.createProfile(txtNameOfNewProfile.getText());
		}
		else {
			profile = new ParallelizationParadigmProfile(paradigms.getValue(),
				txtNameOfNewProfile.getText());
		}
		parallelService.addProfile(profile);
		cmbProfiles.getItems().add(profile);
		cmbProfiles.getSelectionModel().select(profile);
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
			ParadigmManager manager = findManager(profile);
			if (manager != null) {
				manager.editProfile(profile);
			}
			parallelService.saveProfiles();
		}
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

	private void closeProfile() {
		try (ParallelizationParadigm paradigm = parallelService.getParadigm()) {
			if (paradigm != null) {
				ParadigmManager manager = findManager(activeProfile);
				if (manager != null) {
					manager.shutdownIfPossible(activeProfile);
				}
			}
		}
	}


	private ParadigmManager findManager(ParallelizationParadigmProfile profile) {
		return paradigmManagerService.getManagers(profile.getParadigmType())
			.stream().filter(m -> m.isProfileSupported(profile)).findAny().orElse(
				null);
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
					if (item == null) {
						return "";
					} else {
						return "" + item.getSimpleName();
					}
				}

				@Override
				public Class<? extends ParallelizationParadigm> fromString(String s) {
					try {
						Class<? extends ParallelizationParadigm> temp = (Class<? extends ParallelizationParadigm>) Class.forName(s);
						return temp;
					} catch (ClassNotFoundException e) {
						return null;
					}
				}
			});
			
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!paradigms.getItems().isEmpty()) {
				paradigms.getSelectionModel().select(0);
			}
		});
	}

	private void initProfile() {
		ParallelizationParadigm paradigm = parallelService.getParadigm();
	
		if (paradigm != null) {
			List<ParadigmManager> managers =
				paradigmManagerService.getManagers(paradigm.getClass());
			if (!managers.isEmpty()) {
				managers.get(0).prepareParadigm(activeProfile, paradigm);
			}
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
			boolean paradigmActive = parallelService.getParadigm()
				.getStatus() == Status.ACTIVE;
			chkActive.setSelected(paradigmActive);
			chkActive.setDisable(false);
		}
		else {
			activeProfile = null;
			txtActiveProfile.setText("");
			txtActiveProfileType.setText("");
			chkActive.setDisable(true);
		}
	}
}
