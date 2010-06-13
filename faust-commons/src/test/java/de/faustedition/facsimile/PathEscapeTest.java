package de.faustedition.facsimile;

import junit.framework.Assert;

import org.junit.Test;

public class PathEscapeTest {

	@Test
	public void escapePaths() {
		Assert.assertEquals("C:\\Dokumente\\ und\\ Einstellungen", FacsimileStore.escapePath("C:\\Dokumente und Einstellungen"));
	}
}
