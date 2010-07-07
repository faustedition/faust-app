package de.faustedition.document;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import de.faustedition.AbstractContextTest;

public class HandPropertiesTest extends AbstractContextTest {

	@Autowired
	private SimpleJdbcTemplate jt;

	@Test
	public void handPropertiesAvailable() {
		Assert.assertTrue("Hand properties available", jt.queryForInt("select count(*) from hand") > 0);
	}
}
