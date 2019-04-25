package cz.it4i.parallel.persistence;

import java.io.Serializable;

import org.scijava.parallel.PersistentParallelizationParadigm.CompletableFutureID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(of = { "commandName", "id" }) class InternalCompletableFutureID implements CompletableFutureID {

	@Getter
	final String commandName;

	final Serializable id;
}