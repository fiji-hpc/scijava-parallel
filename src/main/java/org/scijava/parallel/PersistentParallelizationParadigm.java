
package org.scijava.parallel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PersistentParallelizationParadigm extends
	ParallelizationParadigm
{

	public interface CompletableFutureID extends Serializable {

	}

	List<CompletableFutureID> getIDs(
		List<CompletableFuture<Map<String, Object>>> future);

	List<CompletableFuture<Map<String, Object>>> getByIDs(
		List<CompletableFutureID> ids);

	void purge(List<CompletableFutureID> ids);

	Collection<CompletableFuture<Map<String, Object>>> getAll();

}
