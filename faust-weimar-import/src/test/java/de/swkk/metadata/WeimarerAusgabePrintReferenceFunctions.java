package de.swkk.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/faust-weimar-import-context.xml" })
public class WeimarerAusgabePrintReferenceFunctions {
	@Autowired
	private InventoryDatabase inventoryDatabase;

	@Autowired
	private WeimarerAusgabeFaustRegister faustRegister;

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
