package cz.it4i.parallel;

import java.util.Collection;

import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.ImageJServerParadigm.Host;

public interface MultipleHostParadigm extends ParallelizationParadigm {

	void setHosts(final Collection<Host> hosts);

}
