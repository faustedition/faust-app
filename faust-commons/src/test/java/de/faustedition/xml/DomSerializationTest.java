package de.faustedition.xml;

import org.junit.Test;

public class DomSerializationTest {

	@Test
	public void domSerializer() {
		XmlUtil.serialize(XmlUtil.parse(getClass().getResourceAsStream("/de/faustedition/tei/faust-tei-sample.xml")), System.out);
	}
}
