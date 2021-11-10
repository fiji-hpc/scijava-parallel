
package cz.it4i.parallel.internal.ui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.scijava.parallel.ParallelService;

import cz.it4i.parallel.internal.ParadigmManagerService;
import cz.it4i.swing_javafx_ui.IconHelperMethods;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
				parentStage.initModality(Modality.NONE);
				parentStage.setResizable(false);
				parentStage.setTitle("Paradigm Profiles Configurator");
				parentStage.setScene(scene);
				Image myImage = IconHelperMethods.convertIkonToImage(
					MaterialDesign.MDI_ACCOUNT_SETTINGS_VARIANT);
				parentStage.getIcons().add(myImage);
				parentStage.show();
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
