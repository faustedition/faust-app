package de.faustedition.facsimile;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.Log;

public class FacsimileStoreTest extends AbstractContextTest {

	@Autowired
	private FacsimileStore store;

	@Test
	public void properties() {
		FacsimileProperties properties = store.properties(firstFacsimile());
		Assert.assertNotNull("Properties is not null", properties);
		Log.LOGGER.debug(ToStringBuilder.reflectionToString(properties));
	}

	@Test
	public void tiles() {
		File tiles = store.tiles(firstFacsimile());
		Assert.assertNotNull("Tiles are generated", tiles);
		Log.LOGGER.debug(tiles.getAbsolutePath());
	}

	private Facsimile firstFacsimile() {
		Iterator<Facsimile> facsimiles = store.iterator();
		if (facsimiles.hasNext()) {
			return facsimiles.next();
		}
		throw new AssertionError("No facsimiles in store");
	}
}
