
package cz.it4i.parallel.ui;

import org.scijava.parallel.ParadigmManagerService;
import org.scijava.parallel.ParallelService;

import cz.it4i.swing_javafx_ui.FXFrame;

public class ParadigmScreenWindow extends FXFrame<ParadigmScreenController> {

	private static final long serialVersionUID = 1L;

	public ParadigmScreenWindow(ParallelService service,
		ParadigmManagerService paradigmManagerService)
	{
		super(null, ParadigmScreenController::new);
		getFxPanel().getControl().initWithServices(service,
			paradigmManagerService);
	}


}
