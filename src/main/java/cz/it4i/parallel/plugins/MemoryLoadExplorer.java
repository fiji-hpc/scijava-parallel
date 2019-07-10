package cz.it4i.parallel.plugins;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;

@SuppressWarnings("restriction")
@Plugin(type = Command.class, menuPath = "Plugins>Utilities>Memory Load")
public class MemoryLoadExplorer  implements Command {
	@Parameter(type = ItemIO.OUTPUT)
	private double totalPhysicalMemorySize;

	@Parameter(type = ItemIO.OUTPUT)
	private double freePhysicalMemorySize;
	
	@Parameter(type = ItemIO.OUTPUT)
	private double memoryUtilization;
	
	@Override
	public void run() {
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
				OperatingSystemMXBean.class);
		// Bellow measurements are in bytes:
		totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize();
		
		freePhysicalMemorySize = osBean.getFreePhysicalMemorySize();
		
		double usedPhysicalMemory = totalPhysicalMemorySize - freePhysicalMemorySize;
		
		// Calculate the utilization (in [0.0,1.0] interval):
		memoryUtilization = usedPhysicalMemory/totalPhysicalMemorySize;
	}

}
