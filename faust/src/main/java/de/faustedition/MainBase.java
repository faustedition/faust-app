package de.faustedition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.jetty.util.log.Log;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import de.faustedition.inject.ConfigurationModule;
import de.faustedition.inject.DataAccessModule;
import de.faustedition.inject.WebResourceModule;

public abstract class MainBase implements Runnable {
    protected static DeploymentMode mode = DeploymentMode.DEVELOPMENT;
    protected static boolean debug = false;


    protected static void main(Class<? extends Runnable> clazz, String[] args) throws Exception {
        for (String arg : args) {
            if ("-production".equalsIgnoreCase(arg)) {
                mode = DeploymentMode.PRODUCTION;
            }
            if ("-debug".equalsIgnoreCase(arg)) {
                debug = true;
            }
        }
        
        configureLogger();

        Stage stage = (mode == DeploymentMode.PRODUCTION ? Stage.PRODUCTION : Stage.DEVELOPMENT);
        Injector injector = Guice.createInjector(stage, new Module[] { new ConfigurationModule(), new DataAccessModule(), new WebResourceModule() });
        injector.getInstance(clazz).run();
    }

    protected static void configureLogger() {
        final Level level = (debug ? Level.ALL : Level.WARNING);
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleLogFormatter());
        handler.setLevel(level);

        Logger rootLogger = Logger.getLogger("");
        for (Handler prevHandler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(prevHandler);
        }
        rootLogger.addHandler(handler);

        Log.setLog(new JettyRedirectingLogger());
        for (String interestingLogger : new String[] { "de.faustedition", "com.google.inject", "org.restlet", "org.eclipse.jetty",
                "freemarker" }) {
            Logger.getLogger(interestingLogger).setLevel(level);
        }
    }

    private static class SimpleLogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder msg = new StringBuilder();
            msg.append(String.format("[%40.40s][%7s]: %s\n", record.getLoggerName(), record.getLevel(), record.getMessage()));
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
            this(Logger.getLogger("org.eclipse.jetty"));
        }

        private JettyRedirectingLogger(Logger logger) {
            this.logger = logger;
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
            return new JettyRedirectingLogger(Logger.getLogger(logger.getName() + "." + name));
        }

        @Override
        public String getName() {
            return logger.getName();
        }
    }
}
