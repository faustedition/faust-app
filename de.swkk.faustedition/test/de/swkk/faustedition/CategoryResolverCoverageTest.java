package de.swkk.faustedition;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StringUtils;

public class CategoryResolverCoverageTest {

	private CategoryResolver categoryResolver = new CategoryResolver();

	private InventoryDatabase faustMetadata = new InventoryDatabase();

	@Test
	public void checkFaustMetadata() {
		SortedSet<String> categoryIdentifiers = new TreeSet<String>();
		for (SortedMap<String, String> record : faustMetadata) {
			categoryIdentifiers.addAll(record.keySet());
		}

		SortedSet<String> unresolvedCategories = new TreeSet<String>();
		for (String categoryIdentifier : categoryIdentifiers) {
			if (categoryResolver.resolve(categoryIdentifier) == null) {
				unresolvedCategories.add(categoryIdentifier);
			}
		}
		if (!unresolvedCategories.isEmpty()) {
			Assert.fail(String.format("Cannot resolve {%s}", StringUtils
					.arrayToCommaDelimitedString(unresolvedCategories.toArray())));
		}
	}
}
