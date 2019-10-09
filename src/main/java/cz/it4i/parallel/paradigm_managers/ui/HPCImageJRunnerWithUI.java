
package cz.it4i.parallel.paradigm_managers.ui;



import java.util.List;
import java.util.function.IntConsumer;

import cz.it4i.parallel.paradigm_managers.HPCImageJRunner;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HPCImageJRunnerWithUI extends HPCImageJRunner {

	public HPCImageJRunnerWithUI(List<String> parameters, IntConsumer portWaiting,
		int startPort)
	{
		super(parameters, portWaiting, startPort);
	}

	private Window owner;

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

	@Override
	protected void doReconnect() {
		try (HPCStatusDialog dialog = new HPCStatusDialog(owner, getServerName())) {
			log.debug("close");
			dialog.imageJServerReconnecting();
			super.doReconnect();
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

}
