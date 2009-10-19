package de.faustedition.util;

public class ErrorUtil
{

	public static RuntimeException fatal(String message)
	{
		LoggingUtil.LOG.fatal(message);
		return new RuntimeException(message);
	}

	public static RuntimeException fatal(String message, Throwable cause)
	{
		LoggingUtil.LOG.fatal(message, cause);
		return new RuntimeException(message, cause);
	}
}
