package de.faustedition;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
	public static final Logger LOGGER = LoggerFactory.getLogger(Log.class.getPackage().getName());
	
	public static RuntimeException fatalError(String... messages) {
		String message = createMessage(messages);
		if (message != null) {
			LOGGER.error(message);
		}
		return (message == null ? new RuntimeException() : new RuntimeException(message));
	}

	public static RuntimeException fatalError(Throwable cause, String... messages) {
		String message = createMessage(messages);
		if (message != null) {
			LOGGER.error(message, cause);
		}
		return (message == null ? new RuntimeException(cause) : new RuntimeException(message, cause));
	}

	private static String createMessage(String... messages) {
		if (messages.length == 0) {
			return null;
		} else if (messages.length == 1) {
			return messages[0];
		} else {
			return String.format(messages[0], ArrayUtils.subarray(messages, 1, messages.length));
		}
	}
}
