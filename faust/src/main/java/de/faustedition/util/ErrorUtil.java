package de.faustedition.util;

import java.util.Arrays;

public class ErrorUtil
{

	public static RuntimeException fatal(String... messages)
	{
		String message = createMessage(messages);
		if (message != null)
		{
			LoggingUtil.LOG.fatal(messages);
		}
		return (message == null ? new RuntimeException() : new RuntimeException(message));
	}

	public static RuntimeException fatal(Throwable cause, String... messages)
	{
		String message = createMessage(messages);
		if (message != null)
		{
			LoggingUtil.LOG.fatal(message);
		}
		return (message == null ? new RuntimeException(cause) : new RuntimeException(message, cause));
	}

	private static String createMessage(String... messages)
	{
		if (messages.length == 0)
		{
			return null;
		}
		else if (messages.length == 1)
		{
			return messages[0];
		}
		else
		{
			return String.format(messages[0], (Object[]) Arrays.copyOfRange(messages, 1, messages.length));
		}
	}
}
