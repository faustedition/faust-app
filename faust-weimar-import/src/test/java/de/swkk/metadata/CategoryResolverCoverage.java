package de.swkk.metadata;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/import-application-context.xml" })
public class CategoryResolverCoverage {

	@Autowired
	private CategoryResolver categoryResolver;

	@Autowired
	@Qualifier("faustMetadataRecordSet")
	private AllegroRecordSet faustMetadata;

	@Autowired
	@Qualifier("editionMetadataRecordSet")
	private AllegroRecordSet editionMetadata;

	@Autowired
	@Qualifier("paralipomenaMetadataRecordSet")
	private AllegroRecordSet paralipomenaMetadata;

	@Test
	public void checkFaustMetadata() {
		checkCoverage(faustMetadata);
	}

	@Test
	public void checkEditionMetadata() {
		checkCoverage(editionMetadata);
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
