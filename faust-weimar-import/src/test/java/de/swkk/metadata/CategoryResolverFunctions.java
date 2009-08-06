package de.swkk.metadata;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/import-application-context.xml" })
public class CategoryResolverFunctions {
	@Autowired
	private CategoryResolver categoryResolver;

	@Test
	public void resolveExistingCategories() {
		Assert.assertNotNull(categoryResolver.resolve("06"));
		Assert.assertNotNull(categoryResolver.resolve("06a"));
		Assert.assertNotNull(categoryResolver.resolve("06b"));
	}

	@Test
	public void resolveTrimmedCategories() {
		Assert.assertNotNull(categoryResolver.resolve("   06   "));
		Assert.assertNotNull(categoryResolver.resolve("20 "));
		Assert.assertNotNull(categoryResolver.resolve(" 06b"));
	}

	@Test
	public void resolveNonExistingCategories() {
		Assert.assertNull(categoryResolver.resolve("1000"));
		Assert.assertNull(categoryResolver.resolve("0"));
		Assert.assertNull(categoryResolver.resolve("2"));
	}

	@Test
	public void dumpCategories() {
		for (AllegroRecord record : categoryResolver) {
			for (String categoryField : record.keySet()) {
				System.out.println(categoryField + ": " + record.get(categoryField));
			}
		}
	}
}
