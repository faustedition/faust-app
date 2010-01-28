package de.swkk.metadata;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.faustedition.util.LoggingUtil;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;
import de.swkk.metadata.inventory.FaustInventory;

public class ArchiveDatabaseAccess {
	private ArchiveDatabase archiveDatabase;
	private FaustInventory faustInventory;

	@Before
	public void setUp() throws Exception {
		archiveDatabase = ArchiveDatabase.parse();
		faustInventory = FaustInventory.parse();
	}

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

		LoggingUtil.LOG.info(String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}

}
