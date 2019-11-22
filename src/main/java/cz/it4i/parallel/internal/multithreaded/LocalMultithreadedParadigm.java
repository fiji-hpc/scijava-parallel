
package cz.it4i.parallel.internal.multithreaded;

import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Plugin;

import cz.it4i.parallel.internal.AbstractBaseRPCParadigmImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Plugin(type = ParallelizationParadigm.class)
public class LocalMultithreadedParadigm extends AbstractBaseRPCParadigmImpl {

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
			addWorker(new LocalMultithreadedPluginWorker());
		}
	}

}
