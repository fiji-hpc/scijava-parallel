
package cz.it4i.parallel.ui;

import java.io.Closeable;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

class HPCStatusDialog implements Closeable {

	private Stage dialog;
	private Label label;
	private String serverName;

	HPCStatusDialog(Window parent, String serverName) {
		this.serverName = serverName;
		Runnable runner = () -> {
			BorderPane panel = new BorderPane();
			this.dialog = new Stage(StageStyle.UNDECORATED);
			Scene scene = new Scene(panel, 310, 80);
			dialog.initOwner(parent);
			dialog.setScene(scene);
			this.label = new Label("Waiting for job to be scheduled.");
			panel.setCenter(label);
			dialog.setResizable(false);
		};
		runInternally(runner);
	}

	void imageJServerReconnecting() {
		Runnable runner = () -> {
			this.label.setText("Waiting for the " + serverName + " to reconnect.");
			dialog.show();
		};
		runInternally(runner);
	}

	void imageJServerStarting() {
		Runnable runner = () -> {
			this.label.setText("Waiting for the " + serverName + " to start.");
			dialog.show();
		};
		runInternally(runner);
	}

	void imageJServerStopping() {
		Runnable runner = () -> {
			label.setText("Waiting for the server to stop.");
			dialog.show();
		};

		runInternally(runner);
	}

	@Override
	public void close() {
		Runnable runner = () -> {
			dialog.hide();
		};
		runInternally(runner);
	}

	private void runInternally(Runnable run) {
		JavaFXRoutines.runOnFxThread(run);
	}
}
