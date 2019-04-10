package cz.it4i.parallel.persistence;

import com.google.common.collect.Streams;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cz.it4i.parallel.DefaultParameterProcessor;
import cz.it4i.parallel.ParallelWorker;
import cz.it4i.parallel.ParallelizationParadigmConverter;
import cz.it4i.parallel.ParameterProcessor;
import cz.it4i.parallel.ParameterTypeProvider;
import cz.it4i.parallel.plugins.RequestBrokerServiceCallCommand;
import cz.it4i.parallel.plugins.RequestBrokerServiceGetResultCommand;
import lombok.RequiredArgsConstructor;


public class RequestBrokerServiceParameterProvider
{

	private final ParameterTypeProvider typeProvider;


	private final Map<Class<?>, ParallelizationParadigmConverter<?>> converters;

	private final Map<Object, ParameterProcessor> requestID2processor =
		Collections.synchronizedMap(new HashMap<>());

	public ParameterProcessor constructProvider(String command,
		ParallelWorker pw)
	{
		if (command.equals(
			RequestBrokerServiceCallCommand.class.getCanonicalName()))
		{
			return new PCallCommandProcessor(pw);
		}
		else if (command.equals(
			RequestBrokerServiceGetResultCommand.class.getCanonicalName()))
		{
			return new PGetResultProcessor();
		}
		return null;
	}

	public RequestBrokerServiceParameterProvider(
		ParameterTypeProvider typeProvider,
		Map<Class<?>, ParallelizationParadigmConverter<?>> mappers)
	{
		this.typeProvider = typeProvider;
		this.converters = mappers;
	}

	@RequiredArgsConstructor
	private class PCallCommandProcessor implements ParameterProcessor {
	
		private final ParallelWorker worker;
		private List<DefaultParameterProcessor> processors;
	
		@Override
		public Map<String, Object> processInputs(final Map<String, Object> inputs) {
	
			@SuppressWarnings({ "unchecked" })
			List<Map<String, Object>> processing = (List<Map<String, Object>>) inputs
				.get("inputs");
			processors = IntStream.range(0, processing
				.size()).mapToObj(__ -> new DefaultParameterProcessor(typeProvider,
					inputs.get("moduleId").toString(), worker, converters)).collect(
						Collectors.toList());
	
			List<Map<String, Object>> processed = Streams.zip(processing.stream(),
				processors.stream(), (input, processor) -> processor.processInputs(
					input))
				.collect(Collectors.toList());
			Map<String, Object> result = new HashMap<>(inputs);
			result.put("inputs", processed);
			return result;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> processOutputs(Map<String, Object> inputs) {
			Streams.zip(((List<Object>) inputs.get("requestIDs")).stream(), processors
				.stream(), requestID2processor::put)
				.count();
			return inputs;
		}
	
		@Override
		public void close() {
			// TODO Auto-generated method stub
	
		}
	}

	private class PGetResultProcessor implements ParameterProcessor {
	
		private List<Object> requestIDs;

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> processInputs(Map<String, Object> inputs) {
			requestIDs = (List<Object>) inputs.get("requestIDs");
			return inputs;
		}
	
		@Override
		public Map<String, Object> processOutputs(Map<String, Object> outputs) {
			Stream<ParameterProcessor> processors = requestIDs.stream().map(
				requestID2processor::get);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> results = (List<Map<String, Object>>) outputs
				.get("results");
			outputs = new HashMap<>(outputs);

			results = Streams.zip(processors, results.stream(), (proc, output) -> proc
				.processOutputs(output)).collect(Collectors.toList());
			outputs.put("results", results);
			return outputs;
		}
	
		@Override
		public void close() {

		}
	
	}
}
