package de.faustedition.facsimile;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class FacsimileUpdate extends AbstractContextTest {

	@Autowired
	private FacsimileUpdateTask updateTask;

	@Test
	public void runUpdate() throws IOException {
		updateTask.update();
	}
}
