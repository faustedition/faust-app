package de.faustedition.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingUtil {
	private static final Log logger = LogFactory.getLog("de.faustedition");

	public static void debug(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	public static void info(String message) {
		if (logger.isInfoEnabled()) {
			logger.info(message);
		}
	}

	public static void error(String message, Throwable cause) {
		if (logger.isErrorEnabled()) {
			logger.error(message, cause);
		}
	}

	public static void fatal(String message, Throwable cause) {
		if (logger.isFatalEnabled()) {
			logger.fatal(message, cause);
		}
	}
}
