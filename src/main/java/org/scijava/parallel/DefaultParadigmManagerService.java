package org.scijava.parallel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import cz.it4i.parallel.Routines;

@Plugin(type = Service.class)
public class DefaultParadigmManagerService extends AbstractService implements
	ParadigmManagerService
{

	@Parameter
	private PluginService pluginService;

	private List<ParadigmManager> managers =
		new LinkedList<>();


	@Override
	public List<ParadigmManager> getManagers(
		Class<? extends ParallelizationParadigm> paradigmClazz)
	{
		List<ParadigmManager> list = managers.stream().filter(m -> m
			.getSupportedParadigmType().equals(paradigmClazz)).collect(
				Collectors.toList());
		return list;
	}

	@Override
	public void initialize() {
		super.initialize();
		retrieveManagers();
	}

	private void retrieveManagers() {
		managers = pluginService.getPluginsOfType(ParadigmManager.class)
			.stream().map(p -> Routines.supplyWithExceptionHandling(
				p::createInstance)).collect(Collectors
					.toList());
		managers.forEach(man -> context().inject(man));
	}

}
