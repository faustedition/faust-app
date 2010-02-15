package de.faustedition.facsimile;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractModelContextTest;
import de.faustedition.facsimile.FacsimileUpdateTask;

public class FacsimileUpdate extends AbstractModelContextTest {

	@Autowired
	private FacsimileUpdateTask updateTask;

	@Test
	public void runUpdate() {
		updateTask.update();
	}
}
