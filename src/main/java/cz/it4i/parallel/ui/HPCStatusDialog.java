package cz.it4i.parallel.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Closeable;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

class HPCStatusDialog implements Closeable {

	private JDialog dialog;
	private JLabel label;
	private String serverName;

	HPCStatusDialog(Component parent, String serverName) {
		this.serverName = serverName;
		this.dialog = new JOptionPane().createDialog(parent, "Waiting");
		JPanel panel = new JPanel(new BorderLayout());
		dialog.setContentPane(panel);
		this.label = new JLabel("Waiting for job schedule.");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label, BorderLayout.CENTER);
		dialog.setModal(false);
		dialog.setVisible(true);
	}

	void imageJServerStarting() {
		dialog.setVisible(false);
		this.label.setText("Waiting for a " + serverName + " start.");
		dialog.setVisible(true);
	}

	void imageJServerStopping() {
		Runnable runner = () -> {
			label.setText("Waiting for stop.");
			dialog.setVisible(true);
		};

		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runner);
		}
		else {
			runner.run();
		}
	}

	@Override
	public void close() {
		Runnable runner = () -> {
			dialog.setVisible(false);
			dialog.dispose();
		};
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runner);
		}
		else {
			runner.run();
		}
	}
}
