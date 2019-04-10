package cz.it4i.parallel.plugins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scijava.service.SciJavaService;

import cz.it4i.parallel.plugins.DefaultRequestBrokerService.State;

public interface RequestBrokerService extends SciJavaService {

	public void initParallelizationParadigm(List<String> hostName,
		List<Integer> ncores);

	public List<Object> call(String commandName,
		List<Map<String, Object>> parameters);

	public List<Map<String, Object>> getResult(List<Object> requestID);

	public List<State> getState(List<Object> requestID);

	public void purge(List<Object> requestID);

	public Collection<Object> getAllRequests();
}
