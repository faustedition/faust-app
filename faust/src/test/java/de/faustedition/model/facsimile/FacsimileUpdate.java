package de.faustedition.model.facsimile;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;

public class FacsimileUpdate extends AbstractModelContextTest {

	@Autowired
	private FacsimileUpdateTask updateTask;

	@Test
	public void runUpdate() {
		updateTask.update();
	}
}
