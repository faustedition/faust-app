package de.faustedition.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.repository.DataRepository;

public class ModelSetup extends AbstractModelContextTest {

	@Autowired
	private DataRepository dataRepository;

	@Test
	public void storageSetup() throws Exception {
		Assert.assertNotNull(dataRepository.getRepository());
	}
}
