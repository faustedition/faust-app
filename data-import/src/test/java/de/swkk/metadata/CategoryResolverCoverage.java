package de.swkk.metadata;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/faust-weimar-import-context.xml" })
public class CategoryResolverCoverage {

	private CategoryResolver categoryResolver;
	private InventoryDatabase faustInventory;
	private ParalipomenaMapping paralipomenaMetadata;
	private WaPrintMapping waRegister;

	@Before
	public void setUp() throws Exception {
		categoryResolver = CategoryResolver.parse();
		faustInventory = InventoryDatabase.parse();
		paralipomenaMetadata = ParalipomenaMapping.parse();
		waRegister = WaPrintMapping.parse();
	}
	
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

	private void checkCoverage(AllegroRecordSet recordSet) {
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
			Assert.fail(String.format("Cannot resolve {%s}", StringUtils
					.arrayToCommaDelimitedString(unresolvedCategories.toArray())));
		}
	}
}
