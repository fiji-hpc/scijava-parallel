
package cz.it4i.parallel;

import java.util.Map;

import org.scijava.convert.Converter;

public class DefaultParameterProcessor extends ParameterProcessor {

	private Map<Class<?>, ParallelizationParadigmConverter<?>> converters;

	public DefaultParameterProcessor(ParameterTypeProvider typeProvider,
		String commandName, ParallelWorker servingWorker,
		Map<Class<?>, ParallelizationParadigmConverter<?>> converters)
	{
		super(typeProvider, commandName, servingWorker);
		this.converters = converters;
	}

	@Override
	protected <T> Converter<Object, T> construcConverter(Class<T> expectedType,
		ParallelWorker servingWorker)
	{
		@SuppressWarnings("unchecked")
		ParallelizationParadigmConverter<T> result =
			(ParallelizationParadigmConverter<T>) converters.get(expectedType);
		if (result != null) {
			return result.cloneForWorker(servingWorker);
		}
		return null;
	}

}
