package de.faustedition;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.util.ClientList;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.faustedition.tei.TeiEncodingReporter;
import de.faustedition.tei.TeiValidator;

public class Server extends Runtime implements Runnable {
	private final RuntimeMode runtimeMode;
	private final String contextPath;
	private final FaustApplication application;
	private final Logger logger;
	private final TeiValidator validator;
	private final TeiEncodingReporter encodingReporter;

	@Inject
	public Server(RuntimeMode runtimeMode, @Named("ctx.path") String contextPath, FaustApplication application,
			TeiValidator validator, TeiEncodingReporter encodingReporter, Logger logger) {
		this.runtimeMode = runtimeMode;
		this.contextPath = contextPath;
		this.application = application;
		this.validator = validator;
		this.encodingReporter = encodingReporter;
		this.logger = logger;
	}

	public static void main(String[] args) throws Exception {
		main(Server.class, args);
	}

	@Override
	public void run() {
		try {
			logger.info("Starting Faust-Edition in " + runtimeMode + " mode");

			schedulePeriodicTasks();
			startWebserver();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void schedulePeriodicTasks() throws Exception {
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		for (Runnable task : new Runnable[] { validator, encodingReporter }) {
			logger.info("Scheduling " + task + " for daily execution; starting in one hour from now");			
			executor.scheduleAtFixedRate(task, 1, 24, TimeUnit.HOURS);
		}
	}

	private void startWebserver() throws Exception {
		final Component component = new Component();
		ClientList clients = component.getClients();
		clients.add(Protocol.FILE);
		clients.add(Protocol.HTTP);
		clients.add(Protocol.HTTPS);
		

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
