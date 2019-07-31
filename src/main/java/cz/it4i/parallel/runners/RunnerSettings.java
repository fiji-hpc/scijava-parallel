package cz.it4i.parallel.runners;

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

	@Getter
	@Setter
	private boolean shutdownOnClose;

}
