/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.parallel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.scijava.command.Command;
import org.scijava.parallel.ParallelizationParadigm;


public interface RPCParadigm extends ParallelizationParadigm {

	default List<Map<String, Object>> runAll(
		Class<? extends Command> commandClazz, List<Map<String, Object>> parameters)
	{
		return runAll(commandClazz.getName(), parameters);
	}

	default List<CompletableFuture<Map<String, Object>>> runAllAsync(
		Class<? extends Command> commandClazz, List<Map<String, Object>> parameters)
	{
		return runAllAsync(commandClazz.getName(), parameters);
	}

	default List<Map<String, Object>> runAll(String commandName,
		List<Map<String, Object>> parameters)
	{
		List<CompletableFuture<Map<String, Object>>> futures = runAllAsync(
			commandName, parameters);

		return futures.stream().map(f -> {
			try {
				return f.get();
			}
			catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new SciJavaParallelRuntimeException(exc);
			}
			catch (ExecutionException exc) {
				throw new SciJavaParallelRuntimeException(exc);
			}
		}).collect(Collectors.toList());
	}

	List<CompletableFuture<Map<String, Object>>> runAllAsync(String commandName,
		List<Map<String, Object>> parameters);
}
