package de.faustedition.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtil {
	private static final Logger logger = Logger.getLogger("de.faustedition");

	public static void log(Level level, String message) {
		if (logger.isLoggable(level)) {
			logger.log(level, message);
		}
	}

	public static void log(Level level, String message, Throwable cause) {
		if (logger.isLoggable(level)) {
			logger.log(level, message, cause);
		}
	}
}
