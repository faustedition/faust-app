package de.swkk.metadata;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.faustedition.util.LoggingUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/import-application-context.xml" })
public class InventoryDatabaseAccess {

	@Autowired
	private InventoryDatabase inventoryDatabase;

	@Autowired
	private ArchiveDatabase archiveDatabase;

	@Test
	public void callNumbersGiven() {
		for (AllegroRecord record : inventoryDatabase) {
			if (inventoryDatabase.getCallNumber(record) == null) {
				inventoryDatabase.dump(record, System.out);
				Assert.fail(String.format("Record %d without call number"));
			}

		}
	}

	@Test
	public void callNumbersWellformed() {
		Set<Integer> portfolioSet = new TreeSet<Integer>();
		Set<String> subPortfolioSet = new TreeSet<String>();
		Set<Integer> fileSet = new TreeSet<Integer>();
		Set<String> subFileSet = new TreeSet<String>();
		Set<String> contentSet = new TreeSet<String>();

		for (AllegroRecord record : inventoryDatabase) {
			GSACallNumber callNumber = inventoryDatabase.getCallNumber(record);
			portfolioSet.add(callNumber.getPortfolio() == null ? 25 : callNumber.getPortfolio());
			subPortfolioSet.add(callNumber.getSubPortfolio());
			fileSet.add(callNumber.getFile());
			if (callNumber.getSubFile() != null) {
				subFileSet.add(callNumber.getSubFile());
			}
			if (callNumber.getContent() != null) {
				contentSet.add(callNumber.getContent());
			}

		}

		LoggingUtil.LOG.info(String.format("Portfolios: { %s }", StringUtils.join(portfolioSet, ", ")));
		LoggingUtil.LOG.info(String.format("Sub-Portfolios: { %s }", StringUtils.join(subPortfolioSet, ", ")));
		LoggingUtil.LOG.info(String.format("Files: { %s }", StringUtils.join(fileSet, ", ")));
		LoggingUtil.LOG.info(String.format("Sub-Files: { %s }", StringUtils.join(subFileSet, ", ")));
		LoggingUtil.LOG.info(String.format("Content: { %s }", StringUtils.join(contentSet, ", ")));
	}

	@Test
	public void recordsMissingInArchiveDatabase() {
		Set<GSACallNumber> missingCallNumbers = new TreeSet<GSACallNumber>();
		for (AllegroRecord record : inventoryDatabase) {
			GSACallNumber callNumber = inventoryDatabase.getCanonicalCallNumber(record);
			if (archiveDatabase.lookup(callNumber) == null) {
				missingCallNumbers.add(callNumber);
			}
		}

		LoggingUtil.LOG.info(String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}
}
