package cz.it4i.parallel.runners;

import lombok.Builder;
import lombok.Getter;


public class ImageJServerRunnerSettings extends RunnerSettings
{

	@Getter
	private String fijiExecutable;

	@Builder
	private ImageJServerRunnerSettings(String fiji)
	{
		super(true);
		fijiExecutable = fiji;
	}

}
