package de.faustedition;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.util.ClientList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.faustedition.tei.TeiValidator;

public class Server extends Runtime implements Runnable {
	private final RuntimeMode runtimeMode;
	private final String contextPath;
	private final FaustApplication application;
	private final Logger logger;
	private final TeiValidator validator;

	@Inject
	public Server(RuntimeMode runtimeMode, @Named("ctx.path") String contextPath, FaustApplication application,
			TeiValidator validator, Logger logger) {
		this.runtimeMode = runtimeMode;
		this.contextPath = contextPath;
		this.application = application;
		this.validator = validator;
		this.logger = logger;
	}

	public static void main(String[] args) throws Exception {
		main(Server.class, args);
	}

	@Override
	public void run() {
		try {
			logger.info("Starting Faust-Edition in " + runtimeMode + " mode");

			scheduleTeiValidator();
			startWebserver();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected error while starting server", e);
			System.exit(1);
		}
	}

	private void scheduleTeiValidator() throws Exception {
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(validator, 1, 24, TimeUnit.HOURS);
	}

	private void startWebserver() throws Exception {
		final Component component = new Component();
		ClientList clients = component.getClients();
		clients.add(Protocol.FILE);

		switch (runtimeMode) {
		case PRODUCTION:
			component.getServers().add(Protocol.AJP, 8089);
			break;
		case DEVELOPMENT:
			component.getServers().add(Protocol.HTTP, 8080);
			break;
		}

		logger.info("Mounting application under '" + contextPath + "/'");
		component.getDefaultHost().attach(contextPath + "/", application);
		component.getLogService().setEnabled(false);
		component.start();
	}
}
