package de.faustedition;

import de.faustedition.inject.FaustInjector;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.*;

public abstract class Runtime {
	protected static Logger log = Logger.getLogger(Runtime.class.getName());
	protected static Level logLevel = Level.WARNING;

	public static void main(Class<? extends Runnable> clazz, String[] args) {
		Locale.setDefault(new Locale("en", "us"));
		
		File configFile = null;
		for (String arg : args) {
			if ("-debug".equalsIgnoreCase(arg)) {
				logLevel = Level.ALL;
			} else if (configFile == null) {
				configFile = new File(arg);
			}
		}
		configureLogging();
		final Runnable main = FaustInjector.get(configFile).getInstance(clazz);

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
}
