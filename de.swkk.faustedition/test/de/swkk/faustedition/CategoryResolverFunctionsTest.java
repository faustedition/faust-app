package de.swkk.faustedition;

import org.junit.Assert;
import org.junit.Test;

public class CategoryResolverFunctionsTest {
	private CategoryResolver categoryResolver = new CategoryResolver();

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
