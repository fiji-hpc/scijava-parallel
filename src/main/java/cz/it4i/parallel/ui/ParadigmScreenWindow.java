
package cz.it4i.parallel.ui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

import org.scijava.parallel.ParallelService;

import cz.it4i.parallel.internal.ParadigmManagerService;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		try {
			RunnableFuture<Void> f = JavaFXRoutines.runOnFxThread(() -> {
				final Scene scene = new Scene(controller);
				final Stage parentStage = new Stage();
				parentStage.initModality(Modality.APPLICATION_MODAL);
				parentStage.setResizable(false);
				parentStage.setTitle("Pradigm Profiles Manager");
				parentStage.setScene(scene);
				parentStage.showAndWait();
			});
			f.get();
		}
		catch (ExecutionException exc) {
			log.error(exc.getMessage(), exc);
		}
		catch (InterruptedException exc) {
			Thread.currentThread().interrupt();
			log.error(exc.getMessage(), exc);
		}
	}


}
