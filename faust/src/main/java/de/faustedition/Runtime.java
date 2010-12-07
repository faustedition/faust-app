package de.faustedition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.jetty.util.log.Log;

import de.faustedition.inject.FaustInjector;

public abstract class Runtime {
	protected static Logger log = Logger.getLogger(Runtime.class.getName());
	protected static Level logLevel = Level.WARNING;

	public static void main(Class<? extends Runnable> clazz, String[] args) {
		Locale.setDefault(new Locale("en", "us"));
		
		for (String arg : args) {
			if ("-debug".equalsIgnoreCase(arg)) {
				logLevel = Level.ALL;
			}
		}
		configureLogging();
		final Runnable main = FaustInjector.get().getInstance(clazz);

		// FIXME: this is a hack; Guice seems to reset the logging
		// config somehow
		configureLogging();
		dumpLogConfig();

		main.run();
	}

	public static void configureLogging() {
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleLogFormatter());
		handler.setLevel(logLevel);

		final Logger root = Logger.getLogger("");
		for (Handler rootHandler : root.getHandlers()) {
			root.removeHandler(rootHandler);
		}
		root.addHandler(handler);

		for (String interesting : new String[] { "de.faustedition", "com.google.inject", "org.neo4j", "org.goddag4j",
				"freemarker" }) {
			final Logger logger = Logger.getLogger(interesting);
			logger.setLevel(logLevel);

		}
		for (String uninteresting : new String[] { "org.restlet.FaustApplication", "org.eclipse.jetty" }) {
			final Logger logger = Logger.getLogger(uninteresting);
			logger.setLevel(Level.WARNING);

		}
		Log.setLog(new JettyRedirectingLogger());
	}

	public static void dumpLogConfig() {
		final SortedSet<String> configuredLoggers = new TreeSet<String>();
		for (Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames(); loggerNames.hasMoreElements();) {
			configuredLoggers.add(loggerNames.nextElement());
		}
		for (String logger : configuredLoggers) {
			Logger instance = Logger.getLogger(logger);
			log.info("Logger config: '" + logger + "' :: " + instance.getLevel() + " [ delegate: "
					+ instance.getUseParentHandlers() + " ] [ #handlers: " + instance.getHandlers().length
					+ " ]");
		}
	}

	private static class SimpleLogFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			StringBuilder msg = new StringBuilder();
			msg.append(String.format("[%s][%40.40s][%7s]: %s\n", df.format(record.getMillis()), record.getLoggerName(),
					record.getLevel(), record.getMessage()));
			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					msg.append(sw.toString());
				} catch (Exception ex) {
				}
			}
			return msg.toString();
		}

	}

	private static class JettyRedirectingLogger implements org.eclipse.jetty.util.log.Logger {
		private final Logger logger;

		private JettyRedirectingLogger() {
			this.logger = Logger.getLogger("org.eclipse.jetty");
		}

		@Override
		public boolean isDebugEnabled() {
			return logger.isLoggable(Level.FINE);
		}

		@Override
		public void setDebugEnabled(boolean enabled) {
		}

		@Override
		public void info(String msg) {
			logger.info(msg);
		}

		@Override
		public void info(String msg, Object arg0, Object arg1) {
			logger.log(Level.INFO, msg, new Object[] { arg0, arg1 });
		}

		@Override
		public void debug(String msg) {
			logger.fine(msg);
		}

		@Override
		public void debug(String msg, Throwable th) {
			logger.log(Level.FINE, msg, th);

		}

		@Override
		public void debug(String msg, Object arg0, Object arg1) {
			logger.log(Level.FINE, msg, new Object[] { arg0, arg1 });
		}

		@Override
		public void warn(String msg) {
			logger.warning(msg);
		}

		@Override
		public void warn(String msg, Object arg0, Object arg1) {
			logger.log(Level.WARNING, msg, new Object[] { arg0, arg1 });
		}

		@Override
		public void warn(String msg, Throwable th) {
			logger.log(Level.WARNING, msg, th);
		}

		@Override
		public org.eclipse.jetty.util.log.Logger getLogger(String name) {
			return new JettyRedirectingLogger();
		}

		@Override
		public String getName() {
			return logger.getName();
		}
	}
}
