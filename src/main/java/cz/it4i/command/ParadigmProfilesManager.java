package cz.it4i.command;

import net.imagej.ImageJ;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.parallel.ParallelService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.internal.ParadigmManagerService;
import cz.it4i.parallel.ui.ParadigmScreenWindow;

@Plugin(headless = true, type = Command.class,
	menuPath = "Plugins>Scijava parallel>Paradigm Profiles Manager")
public class ParadigmProfilesManager implements Command {

	@Parameter
	private ParallelService parallelService;
	
	@Parameter
	private ParadigmManagerService managerService;
	
	@Override
	public void run() {
		ParadigmScreenWindow window = new ParadigmScreenWindow(parallelService,
			managerService);
		window.openWindow();
	}

	public static void main(String[] args) {
		final ImageJ ij = new ImageJ();
		Context ctx = ij.getContext();
		ij.ui().showUI();
		CommandService service = ctx.getService(CommandService.class);
		service.run(ParadigmProfilesManager.class, true);

	}

}
