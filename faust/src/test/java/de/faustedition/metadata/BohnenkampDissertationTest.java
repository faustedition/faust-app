package de.faustedition.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class BohnenkampDissertationTest extends AbstractContextTest {

	@Autowired
	private BohnenkampDissertationDataService dissertationDataService;

	@Test
	public void read() throws Exception {
		for (BohnenkampParalipomenonTranscription t : dissertationDataService.extractParalipomena()) {
			System.out.println(t.getCallNumber().toString());
		}
	}
}
