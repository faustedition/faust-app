package de.faustedition.model;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelSetup extends AbstractModelContextTest
{

	@Autowired
	private SessionFactory dbSessionFactory;

	@Test
	public void storageSetup() throws Exception
	{
		Assert.assertNotNull(dbSessionFactory);
	}
}
