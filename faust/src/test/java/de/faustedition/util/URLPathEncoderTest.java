package de.faustedition.util;

import org.junit.Assert;
import org.junit.Test;

import de.faustedition.web.freemarker.URLPathEncoder;

public class URLPathEncoderTest
{
	@Test
	public void testPathEncoding() throws Exception
	{
		Assert.assertEquals("/a/b/c", URLPathEncoder.encode("/a/b/c"));
		Assert.assertEquals("/a/b//c", URLPathEncoder.encode("/a/b//c"));
		Assert.assertEquals("/a/b/%26c", URLPathEncoder.encode("/a/b/&c"));
	}
}
