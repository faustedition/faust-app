package de.swkk.metadata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.swkk.metadata.inventory.CategoryResolver;

public class CategoryResolverFunctions {
	private CategoryResolver categoryResolver;

	@Before
	public void setUp() throws Exception {
		categoryResolver = new CategoryResolver();
	}

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
