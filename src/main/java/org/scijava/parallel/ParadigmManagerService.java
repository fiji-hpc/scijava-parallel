package org.scijava.parallel;

import java.util.List;

import org.scijava.service.SciJavaService;

public interface ParadigmManagerService extends SciJavaService {

	List<ParadigmManager> getManagers(
		Class<? extends ParallelizationParadigm> paradigmType);

}
