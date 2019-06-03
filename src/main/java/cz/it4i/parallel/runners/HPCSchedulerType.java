package cz.it4i.parallel.runners;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import cz.it4i.parallel.SciJavaParallelRuntimeException;

public enum HPCSchedulerType {
		SLURM("Slurm", SlurmHPCSchedulerAdapter.class), PBS("PBS",
			PBSHPCSchedulerAdapter.class);

	private static final Map<String, HPCSchedulerType> string2Type = EnumSet
		.allOf(
		HPCSchedulerType.class).stream().collect(Collectors.toMap(v -> v.toString(),
			v -> v));

	public static HPCSchedulerType getByString(String string) {
		return string2Type.get(string);
	}
	private HPCSchedulerType(String name,
		Class<? extends HPCSchedulerAdapter> adapterType)
	{
		this.name = name;
		this.adapterType = adapterType;
	}

	private final String name;

	private final Class<? extends HPCSchedulerAdapter> adapterType;

	@Override
	public String toString() {
		return name;
	}

	public HPCSchedulerAdapter create() {
		try {
			return adapterType.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException exc)
		{
			throw new SciJavaParallelRuntimeException(exc);
		}
	}
}
