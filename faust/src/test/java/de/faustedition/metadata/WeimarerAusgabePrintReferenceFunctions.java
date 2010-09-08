package de.faustedition.metadata;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;


public class WeimarerAusgabePrintReferenceFunctions extends AbstractContextTest {
	@Autowired
	private InventoryDatabase inventoryDatabase;
	
	@Autowired
	private WaPrintMapping waRegister;

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
