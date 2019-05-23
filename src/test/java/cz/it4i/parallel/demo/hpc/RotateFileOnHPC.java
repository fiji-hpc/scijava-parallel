
package cz.it4i.parallel.demo.hpc;

import java.io.IOException;

import net.imagej.ImageJ;

import org.scijava.parallel.ParallelizationParadigm;

import cz.it4i.parallel.AbstractImageJServerRunner;
import cz.it4i.parallel.demo.RotateFile;
import cz.it4i.parallel.ui.HPCImageJServerRunnerWithUI;
import cz.it4i.parallel.utils.TestParadigm;

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
