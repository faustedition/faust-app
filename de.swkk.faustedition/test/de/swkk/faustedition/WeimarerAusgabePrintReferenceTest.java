package de.swkk.faustedition;

import org.junit.Before;
import org.junit.Test;

public class WeimarerAusgabePrintReferenceTest {
	private InventoryDatabase inventoryDatabase = new InventoryDatabase();

	private WeimarerAusgabeFaustRegister faustRegister = new WeimarerAusgabeFaustRegister();

	@Before
	public void setUp() {
		inventoryDatabase.setFaustRegister(faustRegister);
	}

	@Test
	public void listRegister() {
		for (String description : faustRegister.getDescriptionSet()) {
			System.out.println(description);
		}
	}

	@Test
	public void dumpReferences() {
		int referenceCount = 0;
		for (AllegroRecord record : inventoryDatabase) {
			String faustReference = inventoryDatabase.getFaustReference(record);
			if (faustReference != null) {
				referenceCount++;
			}
			System.out.printf("%s: %s\n", inventoryDatabase.getCallNumber(record), faustReference);
		}

		System.out.printf("Reference ratio: %d : %d\n", referenceCount, inventoryDatabase.size());
	}
}
