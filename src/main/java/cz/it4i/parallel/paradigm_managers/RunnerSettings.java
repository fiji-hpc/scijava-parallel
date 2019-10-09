package cz.it4i.parallel.paradigm_managers;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class RunnerSettings
	implements Serializable, Cloneable
{

	private static final long serialVersionUID = 6198010915299848014L;

	@Getter
	@Setter
	private boolean shutdownOnClose;

}
