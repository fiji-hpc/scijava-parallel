
package cz.it4i.parallel.demo.hpc;

import java.io.IOException;

import net.imagej.ImageJ;

import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.AbstractImageJServerRunner;
import cz.it4i.parallel.demo.RotateFile;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.utils.TestParadigm;

/**
 * Demonstration example showing basic usage of ParalellizationParadigm with
 * ImageJ server started in HPC cluster. It downloads a picture (Lena) and
 * rotate it for 170 and 340 degree. Result is stored into directory 'output'
 * located in working directory. Result is processed asynchronously.
 * 
 * @formatter:off
 * 
 * You can use settings: 
 * 1. Host name: salomon.it4i.cz
 * 2. Remote directory with Fiji: /scratch/work/project/open-15-12/apps/Fiji.app-scijava-parallel
 * 3. Remote ImageJ command: fiji-linux64
 * 4. Number of nodes: 1
 * 5. Number of CPUs per node: 24
 * @formatter:on
 * 
 * @author koz01
 */
public class RotateFileOnHPC {

	public static void main(String[] args) throws IOException {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		AbstractImageJServerRunner runner = HPCImageJServerRunnerWithUI.gui( ij.context() );

		try(ParallelizationParadigm paradigm = new TestParadigm( runner, ij.context() )) {
			RotateFile.callRemotePlugin(paradigm);
		}
	}
}
