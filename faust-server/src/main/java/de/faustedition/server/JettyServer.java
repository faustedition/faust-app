package de.faustedition.server;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyServer
{
	private static final Logger logger = Logger.getLogger("de.faustedition");

	public static void main(String[] args)
	{
		try
		{
			String faustWebapp = System.getProperty("faust.webapp");
			if (faustWebapp == null)
			{
				logger.severe("No system property 'faust.webapp' given");
				System.exit(-1);
			}

			File faustWebappFile = new File(faustWebapp);
			if (!faustWebappFile.canRead())
			{
				logger.severe(String.format("Cannot read webapp source '%s'", faustWebappFile.getAbsolutePath()));
				System.exit(-2);
			}

			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setPort(9999);
			connector.setForwarded(true);

			final Server server = new Server();
			server.addConnector(connector);
			server.setHandler(new WebAppContext(faustWebappFile.toURI().toString(), "/"));
			server.setStopAtShutdown(true);
			server.start();
			server.join();
		} catch (Exception e)
		{
			logger.log(Level.SEVERE, "Error starting Jetty server", e);
			System.exit(-3);
		}
	}
}
