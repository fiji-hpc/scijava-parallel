/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this project.
 ******************************************************************************/
package org.scijava.parallel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.Service;

import cz.it4i.parallel.internal.ParadigmManagerService;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider implementation of {@link ParallelService}
 * 
 * @author Petr Bainar
 */
@Slf4j
@Plugin(type = Service.class)
public class DefaultParallelService extends
	AbstractSingletonService<ParallelizationParadigm> implements ParallelService
{

	@Parameter
	private PrefService prefService;

	@Parameter
	private ParadigmManagerService paradigmManagerService;

	/** List of parallelization profiles */
	private List<ParallelizationParadigmProfile> profiles;

	/** A string constant to be used by {@link PrefService} */
	private static final String PROFILES_PROPERTY_NAME = "profiles";
	
	// -- ParallelService methods --

	@Override
	public <T extends ParallelizationParadigm> T getParadigmOfType(
		Class<T> type)
	{
		ParallelizationParadigm result = getSelectedParadigm();
		if (result != null && type.isInstance(result)) {
			return type.cast(result);
		}
		return null;
	}

	@Override
	public List<ParallelizationParadigmProfile> getProfiles() {
		return profiles;
	}

	@Override
	public void addProfile(final ParallelizationParadigmProfile profile) {
		if(getProfiles().stream().anyMatch(p -> p.getName().equals(profile.getName()))) {
			throw new IllegalArgumentException("Profile with name " + profile
				.getName() + " already exists.");
		}
		profiles.add(profile);
		saveProfiles();
	}

	@Override
	public void saveProfiles() {
		final List<String> serializedProfiles = new LinkedList<>();
		profiles.forEach(p -> serializedProfiles.add(serializeProfile(p)));
		prefService.put(this.getClass(), PROFILES_PROPERTY_NAME,
			serializedProfiles);
	}

	@Override
	public void selectProfile(final String name) {
		ParallelizationParadigmProfile oldSelectedProfile = getProfile();
		ParallelizationParadigm oldSelectedParadigm = getSelectedParadigm();
		profiles.stream().filter(Objects::nonNull).forEach(p -> p.setSelected(p
			.getName().equals(name)));
		if (oldSelectedProfile != null && oldSelectedParadigm != null &&
			oldSelectedProfile != getProfile() && oldSelectedParadigm
				.getStatus() == Status.ACTIVE)
		{
			oldSelectedParadigm.close();
		}
		saveProfiles();
	}

	@Override
	public void deleteProfile(String name) {
		Iterator<ParallelizationParadigmProfile> iterator = profiles.iterator();
		while (iterator.hasNext()) {
			ParallelizationParadigmProfile paradigm = iterator.next();
			if (paradigm.getName().equals(name)) {
				iterator.remove();
			}
		}
		clearProfiles();
		saveProfiles();
	}

	@Override
	public void deleteProfiles() {
		profiles.clear();
		clearProfiles();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		retrieveProfiles();
		Runtime.getRuntime().addShutdownHook(new Thread(this::dispose));
	}

	@Override
	public void dispose() {
		super.dispose();
		getInstances().stream().filter(par -> par.getStatus() == Status.ACTIVE)
			.forEach(par -> par.close());
	}

	// -- Helper methods --

	// -- Service methods --
	
	// -- ParallelService methods --
	
	private ParallelizationParadigm getSelectedParadigm() {
		ParallelizationParadigmProfile selectedProfile = getProfile();
		if (selectedProfile != null) {
	
			final List<ParallelizationParadigm> foundParadigms = getInstances()
				.stream().filter(paradigm -> paradigm.getClass().equals(selectedProfile
					.getParadigmType())).collect(Collectors.toList());
			if (foundParadigms.size() == 1) {
				ParallelizationParadigm paradigm = foundParadigms.get(0);
				if (paradigm.getStatus() == Status.NON_ACTIVE) {
					ParadigmManager manager = paradigmManagerService.getManagers(
						selectedProfile);
					if (manager != null) {
						manager.prepareParadigm(selectedProfile, paradigm);
					}
				}
				return paradigm;
			}
		}
	
		return null;
	}

	private void clearProfiles() {
		prefService.remove(this.getClass(), PROFILES_PROPERTY_NAME);
	}

	private ParallelizationParadigmProfile getProfile() {
		final List<ParallelizationParadigmProfile> selectedProfiles = getProfiles()
				.stream().filter(p -> BooleanUtils.isTrue(p.isSelected())).collect(
				Collectors.toList());
			
		return selectedProfiles.size() == 1 ? selectedProfiles.get(0) : null;
	}

	private void retrieveProfiles() {
		profiles = new LinkedList<>();
		prefService.getList(this.getClass(), PROFILES_PROPERTY_NAME).stream().map(
			this::deserializeProfile).filter(Objects::nonNull).forEach(p -> profiles
				.add(p));
	}

	private String serializeProfile(
		final ParallelizationParadigmProfile profile)
	{
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(profile);
			}
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	private ParallelizationParadigmProfile deserializeProfile(
		final String serializedProfile)
	{
		try {
			final byte[] data = Base64.getDecoder().decode(serializedProfile);
			try (final ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(data)))
			{
				final Object o = ois.readObject();
				if (ParallelizationParadigmProfile.class.isAssignableFrom(o
					.getClass()))
				{
					this.getContext().inject(o);
					return (ParallelizationParadigmProfile) o;
				}
			}
		}
		catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
