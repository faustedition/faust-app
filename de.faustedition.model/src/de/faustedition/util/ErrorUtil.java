package de.faustedition.util;

import java.util.logging.Level;

public class ErrorUtil {

	public static RuntimeException fatal(String message, Throwable cause) {
		LoggingUtil.log(Level.SEVERE, message, cause);
		return new RuntimeException(message, cause);
	}
}
