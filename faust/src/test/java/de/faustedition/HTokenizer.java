package de.faustedition;

import de.faustedition.transcript.TranscriptManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HTokenizer extends Runtime implements Runnable {

	@Autowired
	private TranscriptManager transcriptManager;

	@Autowired
	private Logger logger;

	public static void main(String[] args) throws IOException {
		main(HTokenizer.class, args);
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
