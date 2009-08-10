package de.faustedition.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.faustedition.model.store.ContentStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml" })
public class ModelSetup {

	@Autowired
	private ContentStore contentStore;

	@Test
	public void storageSetup() throws Exception {
		Assert.assertNotNull(contentStore.getRepository());
	}
}
