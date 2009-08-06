package de.swkk.metadata;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

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
public class ArchiveDatabaseAccess {
	@Autowired
	private ArchiveDatabase archiveDatabase;

	@Autowired
	private InventoryDatabase inventoryDatabase;

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

		LoggingUtil.log(Level.INFO, String.format("Missing call numbers: { %s }", StringUtils.join(missingCallNumbers, ", ")));
	}

}
