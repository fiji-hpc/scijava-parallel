package cz.it4i.parallel.internal.plugins;

import java.util.List;
import java.util.Map;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(headless = true, type = Command.class)
public class RequestBrokerServiceCallCommand implements Command {

	@Parameter
	private DefaultRequestBrokerService service;

	@Parameter(type = ItemIO.INPUT)
	private List<Map<String, Object>> inputs;

	@Parameter(type = ItemIO.INPUT)
	private String moduleId;

	@Parameter(type = ItemIO.OUTPUT)
	private List<Object> requestIDs;

	@Override
	public void run() {
		requestIDs = service.call(moduleId, inputs);
	}

}