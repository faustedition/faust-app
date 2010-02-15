package de.faustedition;

import org.junit.Assert;
import org.junit.Test;

import de.faustedition.web.URLPathEncoder;

public class URLPathEncoderTest {
	@Test
	public void testPathEncoding() throws Exception {
		Assert.assertEquals("/a/b/c", URLPathEncoder.encode("/a/b/c"));
		Assert.assertEquals("/a/b//c", URLPathEncoder.encode("/a/b//c"));
		Assert.assertEquals("/a/b/%26c", URLPathEncoder.encode("/a/b/&c"));
	}
}
