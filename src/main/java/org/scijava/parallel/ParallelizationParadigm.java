// TODO: Add copyright stuff

package org.scijava.parallel;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.scijava.command.Command;
import org.scijava.plugin.SingletonPlugin;

// TODO: Add description

public interface ParallelizationParadigm extends SingletonPlugin, Closeable {

	void init();

	List<Map<String,?>> runAll(List<Class<? extends Command>> commands, List<Map<String,?>> parameters);
	
	// -- Closeable methods --

	
	
	@Override
	default public void close() {

	}
}
