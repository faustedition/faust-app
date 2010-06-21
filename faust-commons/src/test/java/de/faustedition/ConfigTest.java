package de.faustedition;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

public class ConfigTest extends AbstractContextTest {
	@Value("#{config['tei.schema.url']}")
	private String teiSchemaUrl;

	@Test
	public void filtering() {
		Log.LOGGER.info(teiSchemaUrl);
		Assert.assertFalse("TEI schema URL has been filtered", teiSchemaUrl.contains("${tei.schema.version}"));
	}
}
