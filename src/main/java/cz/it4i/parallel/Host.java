package cz.it4i.parallel;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.stream.Collectors;


import lombok.Data;

@Data
public class Host {
	private final String name;
	private final int nCores;

	public static List<Host> constructListFromNamesAndCores(List<String> names,
		List<Integer> cores)
	{
		return Streams.zip(names.stream(), cores.stream(), Host::new).collect(
			Collectors.toList());
	}
}