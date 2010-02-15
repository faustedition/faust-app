package de.faustedition.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractModelContextTest;
import de.faustedition.metadata.EncodingStatusManager;

public class EncodingStatusExtraction extends AbstractModelContextTest {

	@Autowired
	private EncodingStatusManager statusManager;

	@Test
	public void runUpdate() {
		statusManager.update();
	}
}
