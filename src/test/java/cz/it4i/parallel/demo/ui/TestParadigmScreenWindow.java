package cz.it4i.parallel.demo.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.parallel.ParadigmManagerService;
import org.scijava.parallel.ParallelService;

import cz.it4i.parallel.ui.ParadigmScreenWindow;

public class TestParadigmScreenWindow {

	public static void main(String[] args) {
		final ImageJ ij = new ImageJ();
		Context ctx = ij.getContext();
		ij.ui().showUI();
		ParallelService parallelService = ctx.getService(ParallelService.class);
		ParadigmManagerService managerService = ctx.getService(
			ParadigmManagerService.class);
		ParadigmScreenWindow window = new ParadigmScreenWindow(parallelService,
			managerService);
		window.setVisible(true);
		window.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(0);
			}
		});
	}
}
