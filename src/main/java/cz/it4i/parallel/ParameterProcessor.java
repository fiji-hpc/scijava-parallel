
package cz.it4i.parallel;

import static cz.it4i.parallel.Routines.supplyWithExceptionHandling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.scijava.convert.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParameterProcessor {

	private final static Logger log = LoggerFactory.getLogger(
		cz.it4i.parallel.ParameterProcessor.class);

	private Map<String, P_AppliedConversion> appliedConversions = new HashMap<>();

	private String commandName;

	private ParameterTypeProvider typeProvider;

	private Object worker;

	public ParameterProcessor(ParameterTypeProvider typeProvider,
		String commandName, Object worker)
	{
		this.commandName = commandName;
		this.typeProvider = typeProvider;
		this.worker = worker;

	}

	public Map<String, Object> processInputs(Map<String, Object> inputs) {
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Object> entry : inputs.entrySet()) {
			result.put(entry.getKey(), doInputConversion(entry));
		}
		return result;
	}

	public Map<String, Object> processOutput(Map<String, Object> inputs) {
		Map<String, Object> result = new HashMap<>();
		for (Entry<String, Object> entry : inputs.entrySet()) {
			result.put(entry.getKey(), doOutputConversion(entry));
		}
		return result;
	}

	protected String getCommandName() {
		return commandName;
	}

	abstract protected <T> Converter<Object, T> construcConverter(
		Class<T> expectedType, Object servingWorker);

	private String getParameterTypeName(String parameter) {
		return typeProvider.provideParameterTypeName(commandName, parameter);
	}

	private Object doInputConversion(Entry<String, Object> parameter) {
		String typeName = getParameterTypeName(parameter.getKey());
		Converter<Object, ?> convertor = construcConverter(Routines
			.supplyWithExceptionHandling(() -> Class.forName(typeName), log,
				"class load"), worker);
		Object value = parameter.getValue();
		if (convertor != null) {
			appliedConversions.put(parameter.getKey(), new P_AppliedConversion(value
				.getClass(), convertor));
			value = convertor.convert(value, Object.class);

		}
		return value;
	}

	private Object doOutputConversion(Entry<String, Object> parameter) {
		Object value = parameter.getValue();
		P_AppliedConversion appliedConversion = appliedConversions.get(parameter
			.getKey());
		if (appliedConversion != null) {
			value = appliedConversion.conversion.convert(value,
				appliedConversion.srcType);
		}
		else {
			String typeName = getParameterTypeName(parameter.getKey());
			Class<?> type = supplyWithExceptionHandling(() -> Class.forName(typeName),
				log, "class load");
			Converter<Object, ?> convertor = construcConverter(type, worker);
			if (convertor != null) {
				value = convertor.convert(value, type);
			}
		}
		return value;
	}

	private class P_AppliedConversion {

		final private Class<?> srcType;
		final private Converter<Object, ?> conversion;

		public P_AppliedConversion(Class<?> srctype,
			Converter<Object, ?> conversion)
		{
			super();
			this.srcType = srctype;
			this.conversion = conversion;
		}

	}
}