package cz.it4i.parallel.plugins;

import java.util.List;
import java.util.Map;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(headless = true, type = Command.class)
public class RequestBrokerServiceGetResultCommand implements Command {

	@Parameter
	private DefaultRequestBrokerService service;

	@Parameter(type = ItemIO.INPUT)
	private List<Object> requestIDs;


	@Parameter(type = ItemIO.OUTPUT)
	private List<Map<String, Object>> results;

	@Override
	public void run() {
		results = service.getResult(requestIDs);
	}

}