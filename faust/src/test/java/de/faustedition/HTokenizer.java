package de.faustedition;

import java.util.logging.Logger;

import com.google.inject.Inject;

import de.faustedition.transcript.TranscriptManager;

public class HTokenizer extends Runtime implements Runnable {

	private final TranscriptManager transcriptManager;
	private final Logger logger;

	public static void main(String[] args) {
		main(HTokenizer.class, args);
		System.exit(0);
	}

	@Inject
	public HTokenizer(TranscriptManager transcriptManager, Logger logger) {
		this.transcriptManager = transcriptManager;
		this.logger = logger;
	}

	@Override
	public void run() {
		try {
			logger.info("Started processing H");
			final FaustURI h = new FaustURI(FaustAuthority.XML, "/transcript/gsa/391098/391098.xml");
			transcriptManager.add(h);
			logger.info("Ended processing H");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
