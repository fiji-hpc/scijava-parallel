package cz.it4i.parallel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scijava.parallel.ParallelizationParadigm;

public interface MultipleHostParadigm extends ParallelizationParadigm {

	void setHosts(final Collection<Host> hosts);

	List<String> getHosts();

	List<Integer> getNCores();

	List<Map<String, Object>> runOnHosts(String commandName,
		Map<String, Object> parameters, List<String> hosts);
}
