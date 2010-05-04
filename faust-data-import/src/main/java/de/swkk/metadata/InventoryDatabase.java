package de.swkk.metadata;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class InventoryDatabase extends AllegroRecordSet {
	private static final Resource DATABASE_RESOURCE = new ClassPathResource("inventory-database.txt", InventoryDatabase.class);
	private static final String FAUST_REFERENCE_FIELD = "11z";

	private CategoryResolver categoryResolver;
	private WaPrintMapping waRegister;

	public static InventoryDatabase parse() throws IOException {
		InventoryDatabase fi = new InventoryDatabase();
		fi.categoryResolver = CategoryResolver.parse();
		fi.waRegister = WaPrintMapping.parse();
		fi.parse(DATABASE_RESOURCE);
		return fi;
	}

	private InventoryDatabase() {
	}

	public GSACallNumber getCallNumber(AllegroRecord record) {
		for (String candidate : new String[] { "08", "089" }) {
			String callNumber = record.get(candidate);
			if (callNumber != null) {
				return new GSACallNumber(callNumber);
			}
		}

		return null;
	}

	public AllegroRecord lookup(GSACallNumber callNumber) {
		for (AllegroRecord record : this) {
			if (callNumber.contains(getCallNumber(record))) {
				return record;
			}
		}
		return null;
	}

	public void dump(AllegroRecord record, PrintStream printStream) {
		for (Map.Entry<String, String> recordEntry : record.entrySet()) {
			printStream.println(String.format("%60s\t %s", categoryResolver.resolve(recordEntry.getKey()), recordEntry
					.getValue()));
		}
	}

	public void dump(PrintStream printStream) {
		for (AllegroRecord record : this) {
			dump(record, printStream);
			printStream.println("--------------------------------------------------------------------------------");
		}

	}

	public String getFaustReference(AllegroRecord record) {
		String referenceField = record.get(FAUST_REFERENCE_FIELD);
		return (StringUtils.isNotBlank(referenceField) ? waRegister.resolve(referenceField) : null);
	}
}
