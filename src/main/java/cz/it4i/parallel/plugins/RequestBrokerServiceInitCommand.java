package cz.it4i.parallel.plugins;

import java.util.List;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(headless = true, type = Command.class)
public class RequestBrokerServiceInitCommand implements Command {

	@Parameter
	private DefaultRequestBrokerService service;

	@Parameter(type = ItemIO.INPUT)
	private List<String> names;

	@Parameter(type = ItemIO.INPUT)
	private List<Integer> ncores;

	@Parameter(type = ItemIO.INPUT)
	private String paradigmClassName;


	@Override
	public void run() {
		service.initParallelizationParadigm(paradigmClassName, names, ncores);
	}

}