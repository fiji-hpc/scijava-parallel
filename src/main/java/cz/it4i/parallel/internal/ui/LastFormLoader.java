package cz.it4i.parallel.internal.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.scijava.prefs.PrefService;

import cz.it4i.parallel.paradigm_managers.RunnerSettings;
import lombok.extern.slf4j.Slf4j;

//Code responsible for loading and storing the last used form values:
@Slf4j
class LastFormLoader<S extends RunnerSettings> {
	
	private String formStorage;
	
	private PrefService prefService;
	
	private Class<?> formClass;
	
	public LastFormLoader(PrefService newPrefService, String newFormName, Class<?> newFormClass) {
		this.prefService = newPrefService;
		this.formStorage = newFormName;
		this.formClass = newFormClass;
	}

	public void storeLastForm(S form) {
		String serializedForm = serializeForm(form);
		prefService.put(this.formClass, formStorage, serializedForm);
	}

	public S loadLastForm() {
		try {
			String tempString = prefService.get(this.formClass, formStorage);
			return deserializeForm(tempString);
		}
		catch (Exception exc) {
			log.error(exc.getMessage());
		}
		return null;
	}

	private String serializeForm(final S form) {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
				oos.writeObject(form);
			}
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		}
		catch (final Exception e) {
			log.error("serialization " + e.getMessage(), e);
		}
		return null;
	}

	private S deserializeForm(
		final String serializedForm)
	{
		try {
			final byte[] data = Base64.getDecoder().decode(serializedForm);
			try (final ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(data)))
			{
				@SuppressWarnings("unchecked")
				S result = (S) ois.readObject();
				return result;
			}
		}
		catch (final Exception e) {
			log.error("deserialization - " + e.getMessage());
		}
		return null;
	}
	
}
