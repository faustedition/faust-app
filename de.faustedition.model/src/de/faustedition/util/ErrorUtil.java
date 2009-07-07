package de.faustedition.util;


public class ErrorUtil {

	public static RuntimeException fatal(String message, Throwable cause) {
		LoggingUtil.fatal(message, cause);
		return new RuntimeException(message, cause);
	}
}
