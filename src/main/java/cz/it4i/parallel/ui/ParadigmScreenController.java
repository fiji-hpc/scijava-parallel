package cz.it4i.parallel.ui;

import java.util.List;
import java.util.Optional;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParadigmManagerService;
import org.scijava.parallel.ParallelService;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.parallel.ParallelizationParadigmProfile;
import org.scijava.plugin.PluginInfo;

import cz.it4i.parallel.imagej.server.ImageJServerParadigm;
import cz.it4i.parallel.runners.ParadigmProfileUsingRunner;
import cz.it4i.swing_javafx_ui.CloseableControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class ParadigmScreenController extends Pane implements CloseableControl {

	@FXML
	private ComboBox<Class<? extends ParallelizationParadigm>> paradigms;
	
	@FXML
	private ComboBox<ParallelizationParadigmProfile> profiles;

	@FXML
	private Button btnCreate;

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
		ParadigmManager manager = selectProperManager(paradigmManagerService
			.getManagers(paradigms.getValue()));
		ParallelizationParadigmProfile profile;
		if (manager != null) {
			profile = manager.createProfile(txtNameOfNewProfile.getText());
		}
		else {
			profile = new ParallelizationParadigmProfile(paradigms.getValue(),
				txtNameOfNewProfile.getText());
		}
		parallelService.addProfile(profile);
		profiles.getItems().add(profile);
		profiles.getSelectionModel().select(profile);
	}


	public void initProfile() {
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

	public void closeProfile() {
		try (ParallelizationParadigm paradigm = parallelService.getParadigm()) {
			if (paradigm != null) {
				ParadigmManager manager = findManager(activeProfile);
				if (manager != null) {
					manager.shutdownIfPossible(activeProfile);
				}
			}
		}
	}

	public void profileSelected() {
		txtProfileType.setText(profiles.getSelectionModel().getSelectedItem()
			.getParadigmType().getSimpleName());
	}

	public void selectProfile() {
		if (!profiles.getSelectionModel().isEmpty()) {
			parallelService.selectProfile(profiles.getSelectionModel()
				.getSelectedItem().toString());
			updateActiveProfile();
		}
	}

	public void editProfile() {
		if (!profiles.getSelectionModel().isEmpty()) {
			ParallelizationParadigmProfile profile = profiles.getSelectionModel().getSelectedItem();
			ParadigmManager manager = findManager(profile);
			if (manager != null) {
				manager.editProfile(profile);
			}
			parallelService.saveProfiles();
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
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!paradigms.getItems().isEmpty()) {
				paradigms.getSelectionModel().select(0);
			}
		});
	}

	private void initProfiles() {
		for (ParallelizationParadigmProfile profile : parallelService
			.getProfiles())
		{
			profiles.getItems().add(profile);
		}
		JavaFXRoutines.runOnFxThread(() -> {
			if (!profiles.getItems().isEmpty()) {
				profiles.getSelectionModel().select(0);
			}
		});
	}

	private ParadigmManager selectProperManager(List<ParadigmManager> managers) {
		return managers.stream().filter(m -> m.isProfileSupported(
			new ParadigmProfileUsingRunner(HPCImageJServerRunnerWithUI.class,
				ImageJServerParadigm.class, null))).findAny().orElse(null);
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
		}
		else {
			activeProfile = null;
		}
	}
}
