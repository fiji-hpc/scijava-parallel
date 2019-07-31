// TODO: Add copyright stuff

package org.scijava.parallel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.Service;

import lombok.extern.slf4j.Slf4j;

// TODO: Add description

@Slf4j
@Plugin(type = Service.class)
public class DefaultParallelService extends
	AbstractSingletonService<ParallelizationParadigm> implements ParallelService
{

	@Parameter
	PrefService prefService;

	@Parameter
	private ParadigmManagerService paradigmManagerService;

	/** List of parallelization profiles */
	private List<ParallelizationParadigmProfile> profiles;

	/** A string constant to be used by {@link PrefService} */
	private final String PROFILES = "profiles";
	



	// -- ParallelService methods --

	@Override
	public ParallelizationParadigm getParadigm() {
		final List<ParallelizationParadigmProfile> selectedProfiles =
			getProfiles()
			.stream().filter(p -> p.isSelected().equals(true)).collect(Collectors
				.toList());

		if (selectedProfiles.size() == 1) {

			final List<ParallelizationParadigm> foundParadigms = getInstances()
				.stream().filter(paradigm -> paradigm.getClass().equals(selectedProfiles
					.get(0).getParadigmType())).collect(Collectors.toList());

			if (foundParadigms.size() == 1) {
				return foundParadigms.get(0);
			}
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
		prefService.put(this.getClass(), PROFILES, serializedProfiles);
	}

	@Override
	public void selectProfile(final String name) {
		ParallelizationParadigm oldActiveParadigm = getParadigm();
		profiles.stream().filter(Objects::nonNull).forEach(p -> p.setSelected(p
			.getName().equals(name)));
		if (oldActiveParadigm != null && oldActiveParadigm != getParadigm()) {
			oldActiveParadigm.close();
		}
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
		if (getParadigm().getStatus() == Status.ACTIVE) {
			getParadigm().close();
		}
	}

	// -- Helper methods --

	// -- Service methods --
	
	private void clearProfiles() {
		prefService.remove(this.getClass(), PROFILES);
	}

	private void retrieveProfiles() {
		profiles = new LinkedList<>();
		prefService.getList(this.getClass(), PROFILES).stream().map(
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
					return (ParallelizationParadigmProfile) o;
				}
			}
		}
		catch (final Exception e) {
			// TODO: Proper error handling
		}
		return null;
	}

}
