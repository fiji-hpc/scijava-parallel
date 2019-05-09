package cz.it4i.parallel.persistence;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.scijava.parallel.PersistentParallelizationParadigm.CompletableFutureID;

import cz.it4i.parallel.RemoteDataHandler;
import lombok.AllArgsConstructor;

class RemoteDataManager {

	private Map<Serializable, Collection<Runnable>> id2commandMap =
		Collections
		.synchronizedMap(new HashMap<>());
	
	public RemoteDataHandler createProxyDataHandler(RemoteDataHandler handler) {
		return createProxyDataHandler(handler, null);
	}

	public RemoteDataHandler createProxyDataHandler(RemoteDataHandler handler,
		Serializable id)
	{
		return new PRemoteDataHandler(handler, id);
	}

	public void purged(Serializable id) {
		Collection<Runnable> purgeCommands = id2commandMap.remove(id);
		if (purgeCommands != null) {
			purgeCommands.forEach(Runnable::run);
		}
	}

	public void registerPurgeCommand(Serializable id,
		Runnable purgeCommand)
	{
		id2commandMap.computeIfAbsent(id, x -> new LinkedList<>()).add(
			purgeCommand);
	}

	public void setID(RemoteDataHandler handler, Serializable id) {
		if (handler instanceof PRemoteDataHandler) {
			PRemoteDataHandler pDataHandler = (PRemoteDataHandler) handler;
			pDataHandler.id = id;
		}
		else {
			throw new IllegalArgumentException("handler: " + handler +
				"was not created by RemoteDataManager");
		}
	}

	@AllArgsConstructor
	private class PRemoteDataHandler implements RemoteDataHandler {

		RemoteDataHandler handler;

		Serializable id;

		@Override
		public Object importData(Path filePath) {
			return handler.importData(filePath);
		}

		@Override
		public void exportData(Object data, Path filePath) {
			handler.exportData(data, filePath);
		}

		@Override
		public void deleteData(Object ds) {
			RemoteDataManager.this.registerPurgeCommand(id, () -> handler.deleteData(ds));
		}
	}
}
