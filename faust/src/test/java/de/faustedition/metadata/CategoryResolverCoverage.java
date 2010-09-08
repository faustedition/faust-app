package de.faustedition.metadata;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import de.faustedition.AbstractContextTest;

public class CategoryResolverCoverage extends AbstractContextTest {

	@Autowired
	private CategoryResolver categoryResolver;
	@Autowired
	private InventoryDatabase faustInventory;
	@Autowired
	private ParalipomenaMapping paralipomenaMetadata;
	@Autowired
	private WaPrintMapping waRegister;

	@Test
	public void checkFaustMetadata() {
		checkCoverage(faustInventory);
	}

	@Test
	public void checkEditionMetadata() {
		checkCoverage(waRegister);
	}

	@Test
	public void checkParalipomenaMetadata() {
		checkCoverage(paralipomenaMetadata);
	}

	private void checkCoverage(AllegroRecord.Set recordSet) {
		SortedSet<String> categoryIdentifiers = new TreeSet<String>();
		for (SortedMap<String, String> record : recordSet) {
			categoryIdentifiers.addAll(record.keySet());
		}

		SortedSet<String> unresolvedCategories = new TreeSet<String>();
		for (String categoryIdentifier : categoryIdentifiers) {
			if (categoryResolver.resolve(categoryIdentifier) == null) {
				unresolvedCategories.add(categoryIdentifier);
			}
		}
		if (!unresolvedCategories.isEmpty()) {
			Assert.fail(String.format("Cannot resolve {%s}",
					StringUtils.arrayToCommaDelimitedString(unresolvedCategories.toArray())));
		}
	}
}
