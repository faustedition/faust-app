package de.faustedition.facsimile;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class FacsimileTileStoreTest extends AbstractContextTest {

	@Autowired
	private FacsimileTileStore store;

	@Test
	public void buildStore() {
		store.build();
	}

	@Test
	public void properties() {
		FacsimileProperties properties = store.properties(store.all().first());
		Assert.assertNotNull("Properties is not null", properties);
		LOG.debug(ToStringBuilder.reflectionToString(properties));
	}
}
