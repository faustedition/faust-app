package de.swkk.faustedition;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import de.faustedition.util.LoggingUtil;

public class InventoryDatabaseAccessTest {

	private InventoryDatabase inventoryDatabase = new InventoryDatabase();

	private ArchiveDatabase archiveDatabase = new ArchiveDatabase();

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

		LoggingUtil.info(String.format("Portfolios: { %s }", StringUtils.join(portfolioSet, ", ")));
		LoggingUtil.info(String.format("Sub-Portfolios: { %s }", StringUtils.join(subPortfolioSet, ", ")));
		LoggingUtil.info(String.format("Files: { %s }", StringUtils.join(fileSet, ", ")));
		LoggingUtil.info(String.format("Sub-Files: { %s }", StringUtils.join(subFileSet, ", ")));
		LoggingUtil.info(String.format("Content: { %s }", StringUtils.join(contentSet, ", ")));
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

		LoggingUtil.info(String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}
}
