package de.swkk.metadata;

import org.junit.Before;
import org.junit.Test;


public class WeimarerAusgabePrintReferenceFunctions {
	private InventoryDatabase inventoryDatabase;
	private WaPrintMapping waRegister;

	@Before
	public void setUp() throws Exception {
		inventoryDatabase = InventoryDatabase.parse();
		waRegister = WaPrintMapping.parse();
	}
	
	@Test
	public void listRegister() {
		for (String description : waRegister.getDescriptionSet()) {
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
