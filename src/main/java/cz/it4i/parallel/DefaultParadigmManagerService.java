/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.parallel.ParadigmManager;
import org.scijava.parallel.ParallelizationParadigm;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Provided implementation of {@link ParadigmManagerService}.
 * 
 * @author Jan Kožusznik
 */
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
		return managers.stream().filter(m -> m.getSupportedParadigmType().equals(
			paradigmClazz)).collect(Collectors.toList());
	}

	@Override
	public void initialize() {
		super.initialize();
		retrieveManagers();
	}

	private void retrieveManagers() {
		managers = pluginService.getPluginsOfType(ParadigmManager.class).stream()
			.map(p -> InternalExceptionRoutines.supplyWithExceptionHandling(
				p::createInstance)).collect(Collectors.toList());
		managers.forEach(man -> context().inject(man));
	}

}
