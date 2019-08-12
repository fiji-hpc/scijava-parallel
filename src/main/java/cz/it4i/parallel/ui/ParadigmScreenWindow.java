
package cz.it4i.parallel.ui;

import org.scijava.parallel.ParadigmManagerService;
import org.scijava.parallel.ParallelService;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ParadigmScreenWindow {

	private ParadigmScreenController controller;

	public ParadigmScreenWindow(ParallelService service,
		ParadigmManagerService paradigmManagerService)
	{
		this.controller = new ParadigmScreenController();
		controller.initWithServices(service, paradigmManagerService);
		Platform.setImplicitExit(false);
	}

	public void openWindow() {
		JavaFXRoutines.runOnFxThread(() -> {
			final Scene scene = new Scene(controller);
			final Stage parentStage = new Stage();
			parentStage.initModality(Modality.APPLICATION_MODAL);
			parentStage.setResizable(false);
			parentStage.setTitle("Pradigm Profiles Manager");
			parentStage.setScene(scene);
			parentStage.show();
		});
	}


}
