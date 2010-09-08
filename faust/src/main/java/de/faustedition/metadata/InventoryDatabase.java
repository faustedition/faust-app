package de.faustedition.metadata;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class InventoryDatabase extends AllegroRecord.Set {
	private static final long serialVersionUID = 7537781682186027461L;

	private static final String FAUST_REFERENCE_FIELD = "11z";

	@Autowired
	private CategoryResolver categoryResolver;

	@Autowired
	private WaPrintMapping waRegister;

	public InventoryDatabase() {
		setSource(new ClassPathResource("gsa-inventory-database.txt", InventoryDatabase.class));
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
			printStream.println(String.format("%60s\t %s", categoryResolver.resolve(recordEntry.getKey()),
					recordEntry.getValue()));
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
