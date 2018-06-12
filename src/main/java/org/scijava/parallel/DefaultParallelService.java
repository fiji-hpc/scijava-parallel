// TODO: Add copyright stuff

package org.scijava.parallel;

import java.util.List;
import java.util.stream.Collectors;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

// TODO: Add description

@Plugin(type = Service.class)
public class DefaultParallelService extends AbstractSingletonService<ParallelizationParadigm>
		implements ParallelService {
	
	// -- ParallelService methods --
	
	@Override
	public List<ParallelizationParadigm> getParadigms() {
		return getInstances().stream().collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParallelizationParadigm> T getParadigm(
			Class<T> desiredParalellizationParadigm) {
		List<ParallelizationParadigm> matchingParadigms = getInstances().stream()
				.filter(paradigm -> paradigm.getClass().equals(desiredParalellizationParadigm))
				.collect(Collectors.toList());
		
		if (matchingParadigms.size() == 1) {
			return (T) matchingParadigms.get(0);
		}
		
		return null;
	}	
}
