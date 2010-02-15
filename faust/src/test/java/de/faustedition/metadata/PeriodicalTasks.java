package de.faustedition.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractModelContextTest;
import de.faustedition.metadata.EncodingStatusManager;

public class PeriodicalTasks extends AbstractModelContextTest {

	@Autowired
	private EncodingStatusManager encodingStatusManager;

	@Autowired
	private IdentifierManager identifierManager;
	
	@Test
	public void runEncodingStatusUpdate() {
		encodingStatusManager.update();
	}

	@Test
	public void runIdentifierUpdate() {
		identifierManager.update();
	}
}
