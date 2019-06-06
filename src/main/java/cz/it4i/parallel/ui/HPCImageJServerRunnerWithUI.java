
package cz.it4i.parallel.ui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.scijava.Context;

import cz.it4i.parallel.runners.HPCImageJServerRunner;
import cz.it4i.parallel.runners.HPCSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HPCImageJServerRunnerWithUI extends HPCImageJServerRunner {

	private JDialog dialog;
	private JLabel label;


	public HPCImageJServerRunnerWithUI(HPCSettings settings) {
		super(settings);
	}

	public HPCImageJServerRunnerWithUI(HPCSettings settings,
		boolean shutdownOnClose)
	{
		super(settings, shutdownOnClose);
	}

	@Override
	public void start() {
		this.dialog = new JOptionPane().createDialog("Waiting");
		JPanel panel = new JPanel(new BorderLayout());
		dialog.setContentPane(panel);
		this.label = new JLabel("Waiting for job schedule.");
		label.setHorizontalAlignment(SwingConstants.CENTER);

		panel.add(label, BorderLayout.CENTER);
		dialog.setModal(false);
		dialog.setVisible(true);

		imageJServerStarted();
		try  { 
			super.start();
		} finally {
			imageJServerStartFinished();
		}
		imageJServerRunning();
	}

	private void imageJServerStartFinished() {
		dialog.setVisible(false);
	}

	private void imageJServerStarted() {
		log.info("imageJServerStarted");
		dialog.setVisible(false);
		this.label.setText("Waiting for a ImageJ server start.");
		dialog.setVisible(true);
	}

	private void imageJServerRunning() {
		log.info("imageJServerRunning");
		log.info("job: " + getJob().getID() + " started on hosts: " + getJob()
			.getNodes());
	}

	@Override
	public void close() {
		log.info("close");
		label.setText("Waiting for stop.");
		dialog.setVisible(true);
		try {
			super.close();
		}
		finally {
			dialog.setVisible(false);
		}
		dialog.dispose();
		log.info("close done");
	}

	public static HPCImageJServerRunnerWithUI gui(Context context,
		boolean shutdownOnClose)
	{
		return new HPCImageJServerRunnerWithUI(HPCSettingsGui.showDialog(context),
			shutdownOnClose);
	}

	public static HPCImageJServerRunnerWithUI gui(Context context) {
		return gui(context, true);
	}
}
