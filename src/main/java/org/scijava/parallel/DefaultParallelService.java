// TODO: Add copyright stuff

package org.scijava.parallel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.Service;

// TODO: Add description

@Plugin(type = Service.class)
public class DefaultParallelService extends AbstractSingletonService<ParallelizationParadigm>
		implements ParallelService {
	
	@Parameter
	PrefService prefService;
	
	// List of parallelization profiles
	private List<ParallelizationParadigmProfile> profiles;
	
	// -- ParallelService methods --
	
	@Override
	public List<ParallelizationParadigm> getParadigms() {
		return getInstances().stream().collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParallelizationParadigm> T getParadigm(
			final Class<T> desiredParalellizationParadigm) {
		List<ParallelizationParadigm> matchingParadigms = getInstances().stream()
				.filter(paradigm -> paradigm.getClass().equals(desiredParalellizationParadigm))
				.collect(Collectors.toList());
		
		if (matchingParadigms.size() == 1) {
			return (T) matchingParadigms.get(0);
		}
		
		return null;
	}	
	
	@Override
	public void saveProfile(final ParallelizationParadigmProfile profile) {
		
		final List<String> serializedProfiles = new LinkedList<>();
		
		profiles.add(profile);
		profiles.forEach(p -> {
			serializedProfiles.add(serializeProfile(p));
		});
		
		prefService.put(this.getClass(), "profiles", serializedProfiles);
		retrieveProfiles();
		
	}
	
	// -- Service methods --

	@Override
	public void initialize() {

		retrieveProfiles();

	}

	// -- Helper methods --
	
	private void retrieveProfiles() {
		
		profiles = new LinkedList<>();
		prefService.getList(this.getClass(), "profiles").forEach((serializedProfile) -> {
			profiles.add(deserializeProfile(serializedProfile));
		});
		
	}
	
	private String serializeProfile(final ParallelizationParadigmProfile profile) {
		
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(profile);
	        oos.close();
	        return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception e) {
			// TODO: Proper error handling
		}
		return null;
	}
	
	private ParallelizationParadigmProfile deserializeProfile(final String serializedProfile) {
		try {
			final byte[] data = Base64.getDecoder().decode(serializedProfile);
			final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			final Object o = ois.readObject();
			ois.close();
			if (ParallelizationParadigmProfile.class.isAssignableFrom(o.getClass())) {
				return (ParallelizationParadigmProfile) o;
			}
		} catch (Exception e) {
			// TODO: Proper error handling
		}
		return null;
	}
	
}
