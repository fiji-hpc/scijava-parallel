
package cz.it4i.parallel.ui;



import org.scijava.Context;

import cz.it4i.parallel.runners.HPCImageJServerRunner;
import cz.it4i.parallel.runners.HPCSettings;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HPCImageJServerRunnerWithUI extends HPCImageJServerRunner {

	private Window owner;

	public HPCImageJServerRunnerWithUI() {
	}

	public HPCImageJServerRunnerWithUI(HPCSettings settings) {
		super(settings);
	}

	public void initOwnerWindow(Window aOwner) {
		this.owner = aOwner;
	}

	@Override
	public void start() {
		try (HPCStatusDialog dialog = new HPCStatusDialog(owner,
			getServerName()))
		{
			dialog.imageJServerStarting();
			super.start();
			imageJServerRunning();
		}
	}

	@Override
	protected void doCloseInternally(boolean shutdown) {

		try (HPCStatusDialog dialog = new HPCStatusDialog(owner,
			getServerName()))
		{
			log.debug("close");
			dialog.imageJServerStopping();
			super.doCloseInternally(shutdown);
			log.debug("close done");
		}
	}

	protected String getServerName() {
		return "ImageJ server";
	}

	void imageJServerRunning() {
		log.info("job: " + getJob().getID() + " started on hosts: " + getJob()
			.getNodes());
	}
	public static HPCImageJServerRunnerWithUI gui(Context context)
	{
		HPCImageJServerRunnerWithUI result = new HPCImageJServerRunnerWithUI(
			HPCSettingsGui.showDialog(context));
		return result;
	}

}
