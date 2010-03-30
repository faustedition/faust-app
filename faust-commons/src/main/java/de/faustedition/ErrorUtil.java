package de.faustedition;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ErrorUtil.class.getPackage().getName());
	
	public static RuntimeException fatal(String... messages) {
		String message = createMessage(messages);
		if (message != null) {
			LOG.error(message);
		}
		return (message == null ? new RuntimeException() : new RuntimeException(message));
	}

	public static RuntimeException fatal(Throwable cause, String... messages) {
		String message = createMessage(messages);
		if (message != null) {
			LOG.error(message, cause);
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
