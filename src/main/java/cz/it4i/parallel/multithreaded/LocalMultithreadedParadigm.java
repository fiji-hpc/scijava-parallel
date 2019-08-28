
package cz.it4i.parallel.multithreaded;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.parallel.AbstractBaseRPCParadigmImpl;

@Plugin(type = ParallelizationParadigm.class)
public class LocalMultithreadedParadigm extends AbstractBaseRPCParadigmImpl {

	public static final Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.multithreaded.LocalMultithreadedParadigm.class);

	private Integer poolSize;

	// -- LocalMultithreadedParadigm methods --

	public void setPoolSize(final Integer poolSize) {
		this.poolSize = poolSize;
	}

	// -- SimpleOstravaParadigm methods --

	// -- ParallelizationParadigm methods --

	@Override
	public void init() {
		if (poolSize == null) {
			poolSize = 1;
		}
		super.init();
	}

	// -- SimpleOstravaParadigm methods --

	@Override
	protected void initWorkerPool() {
		for (int i = 0; i < poolSize; i++) {
			workerPool.addWorker(new LocalMultithreadedPluginWorker());
		}
	}

}
