package cz.it4i.parallel.internal.plugins;

import java.util.Collection;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(headless = true, type = Command.class)
public class RequestBrokerServiceGetAllCommand implements Command {

	@Parameter
	private DefaultRequestBrokerService service;

	@Parameter(type = ItemIO.OUTPUT)
	private Collection<Object> requestIDs;



	@Override
	public void run() {
		requestIDs = service.getAllRequests();
	}

}