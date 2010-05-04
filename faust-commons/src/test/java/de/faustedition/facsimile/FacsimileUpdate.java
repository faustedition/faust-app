package de.faustedition.facsimile;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class FacsimileUpdate extends AbstractContextTest {

	@Autowired
	private FacsimileUpdateTask updateTask;

	@Test
	public void runUpdate() {
		updateTask.update();
	}
}
