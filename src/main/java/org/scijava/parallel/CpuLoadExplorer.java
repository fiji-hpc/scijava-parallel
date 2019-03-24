
package org.scijava.parallel;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("restriction")
@Plugin(type = Command.class, menuPath = "Plugins>Utilities>CPU Load")
public class CpuLoadExplorer implements Command {

	@Parameter(type = ItemIO.OUTPUT)
	private double processCpuLoad;

	@Parameter(type = ItemIO.OUTPUT)
	private double systemCpuLoad;

	@Override
	public void run() {

		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
			OperatingSystemMXBean.class);

		processCpuLoad = osBean.getProcessCpuLoad();

		systemCpuLoad = osBean.getSystemCpuLoad();

	}

}
