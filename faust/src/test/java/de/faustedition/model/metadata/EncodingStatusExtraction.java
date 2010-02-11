package de.faustedition.model.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;

public class EncodingStatusExtraction extends AbstractModelContextTest {

	@Autowired
	private EncodingStatusManager statusManager;

	@Test
	public void runUpdate() {
		statusManager.update();
	}
}
