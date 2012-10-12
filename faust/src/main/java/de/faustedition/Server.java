package de.faustedition;

import com.google.common.collect.Iterables;
import de.faustedition.tei.TeiValidator;
import de.faustedition.transcript.TranscriptBatchReader;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.util.ClientList;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@org.springframework.stereotype.Component
public class Server extends Runtime implements Runnable, InitializingBean {
	@Autowired
	private Environment environment;

	@Autowired
	private FaustApplication application;

	@Autowired
	private Logger logger;

	@Autowired
	private TeiValidator validator;

	@Autowired
	private TranscriptBatchReader transcriptBatchReader;

	private String contextPath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.contextPath = environment.getRequiredProperty("ctx.path");
	}

	public static void main(String[] args) throws Exception {
		main(Server.class, args);
	}

	@Override
	public void run() {
		try {
			logger.info("Starting Faust-Edition with profiles " + Iterables.toString(Arrays.asList(environment.getActiveProfiles())));

			scheduleTasks();
			startWebserver();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void scheduleTasks() throws Exception {
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		logger.info("Scheduling TEI P5 encoding validator for daily execution; starting in one hour from now");
		executor.scheduleAtFixedRate(validator, 1, 24, TimeUnit.HOURS);

		logger.info("Scheduling transcript batch reader for hourly execution; starting in two minutes now");
		executor.scheduleAtFixedRate(transcriptBatchReader, 55, 55, TimeUnit.MINUTES);
	}

	private void startWebserver() throws Exception {
		final Component component = new Component();
		component.getServers().add(Protocol.HTTP, environment.getRequiredProperty("server.port", Integer.class));

		ClientList clients = component.getClients();
		clients.add(Protocol.FILE);
		clients.add(Protocol.HTTP).setConnectTimeout(4000);
		clients.add(Protocol.HTTPS).setConnectTimeout(4000);

		logger.info("Mounting application under '" + contextPath + "/'");
		component.getDefaultHost().attach(contextPath + "/", application);
		component.getLogService().setEnabled(false);
		component.start();
	}
}
