package de.faustedition.model.facsimile;

import junit.framework.Assert;

import org.junit.Test;

import de.faustedition.facsimile.FacsimileManager;

public class PathEscapeTest {

	@Test
	public void escapePaths() {
		Assert.assertEquals("C:\\Dokumente\\ und\\ Einstellungen", FacsimileManager
				.escapePath("C:\\Dokumente und Einstellungen"));
	}
}
