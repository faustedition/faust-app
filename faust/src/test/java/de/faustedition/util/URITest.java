package de.faustedition.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class URITest {
	@Test
	public void uriConstruction() throws URISyntaxException {
		URI base = new URI("faust", "facsimile", "/GSA/123456/001", null, null);
		System.out.println(base);
		System.out.println(base.resolve("../654321/654"));
		
		System.out.println(new URI("faust", "tei", "/GSA/123456/002.xml", null, null));
	}
	
	@Test
	public void namespaceRelativization() throws URISyntaxException {
		System.out.println(new URI("http://faustedition.net/ns#").resolve("segSpan"));
		System.out.println(new URI("http://faustedition.net/ns").resolve("segSpan"));
	}
}
