package cz.it4i.parallel.plugins;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(headless = true, type = Command.class)
public class RequestBrokerServicePurgeCommand implements Command {

	@Parameter
	private DefaultRequestBrokerService service;

	@Parameter(type = ItemIO.INPUT)
	private List<Object> requestIDs;



	@Override
	public void run() {
		service.purge(requestIDs);
	}

}