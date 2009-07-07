package de.swkk.faustedition;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import de.faustedition.util.LoggingUtil;

public class ArchiveDatabaseAccessTest {
	private ArchiveDatabase archiveDatabase = new ArchiveDatabase();

	private InventoryDatabase inventoryDatabase = new InventoryDatabase();

	@Test
	public void dumpDatabase() {
		for (ArchiveRecord record : archiveDatabase) {
			record.dump(System.out);
			System.out.println("----------------------------------------");
		}
	}

	@Test
	public void callNumbersGiven() {
		for (ArchiveRecord record : archiveDatabase) {
			if (record.getCallNumber() == null) {
				record.dump(System.out);
				Assert.fail(Integer.toString(record.getId()));
			}
		}

	}

	@Test
	public void recordsMissingInInventoryDatabase() {
		Set<GSACallNumber> missingCallNumbers = new TreeSet<GSACallNumber>();
		for (ArchiveRecord record : archiveDatabase) {
			GSACallNumber callNumber = record.getCallNumber();
			if (inventoryDatabase.lookup(callNumber) == null) {
				missingCallNumbers.add(callNumber);
			}
		}

		LoggingUtil.info(String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}

}
