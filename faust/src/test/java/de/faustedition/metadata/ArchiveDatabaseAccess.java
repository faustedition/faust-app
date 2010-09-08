package de.faustedition.metadata;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.Log;

public class ArchiveDatabaseAccess extends AbstractContextTest {
	@Autowired
	private ArchiveDatabaseRecord.List archiveDatabase;
	@Autowired
	private InventoryDatabase faustInventory;

	@Test
	public void dumpDatabase() {
		for (ArchiveDatabaseRecord record : archiveDatabase) {
			record.dump(System.out);
			System.out.println(StringUtils.repeat("=", 80));
		}
	}

	@Test
	public void callNumbersGiven() {
		for (ArchiveDatabaseRecord record : archiveDatabase) {
			if (record.getCallNumber() == null) {
				record.dump(System.out);
				Assert.fail(Integer.toString(record.getId()));
			}
		}

	}

	@Test
	public void recordsMissingInInventoryDatabase() {
		Set<GSACallNumber> missingCallNumbers = new TreeSet<GSACallNumber>();
		for (ArchiveDatabaseRecord record : archiveDatabase) {
			GSACallNumber callNumber = record.getCallNumber();
			if (faustInventory.lookup(callNumber) == null) {
				missingCallNumbers.add(callNumber);
			}
		}

		Log.LOGGER.info(String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}

}
