package de.faustedition.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.store.ContentStore;

public class ModelSetup extends AbstractModelContextTest {

	@Autowired
	private ContentStore contentStore;

	@Test
	public void storageSetup() throws Exception {
		Assert.assertNotNull(contentStore.getRepository());
	}
}
