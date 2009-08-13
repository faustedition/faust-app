package de.swkk.metadata;

import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class InventoryDatabase extends AllegroRecordSet {
	private static final String FAUST_REFERENCE_FIELD = "11z";

	@Autowired
	private CategoryResolver categoryResolver;

	@Autowired
	private WeimarerAusgabeFaustRegister faustRegister;

	public GSACallNumber getCallNumber(AllegroRecord record) {
		for (String candidate : new String[] { "08", "089" }) {
			String callNumber = record.get(candidate);
			if (callNumber != null) {
				return new GSACallNumber(callNumber);
			}
		}

		return null;
	}

	public GSACallNumber getCanonicalCallNumber(AllegroRecord record) {
		GSACallNumber callNumber = getCallNumber(record);
		if (callNumber.getContent() == null) {
			return callNumber;
		}

		String callNumberStr = callNumber.getValue();
		return new GSACallNumber(callNumberStr.substring(0, callNumberStr.length() - callNumber.getContent().length() - 1));
	}

	public AllegroRecord lookup(GSACallNumber callNumber) {
		for (AllegroRecord record : this) {
			if (getCanonicalCallNumber(record).equals(callNumber)) {
				return record;
			}
		}
		return null;
	}

	public void dump(AllegroRecord record, PrintStream printStream) {
		for (Map.Entry<String, String> recordEntry : record.entrySet()) {
			printStream.println(String.format("%60s\t %s", categoryResolver.resolve(recordEntry.getKey()), recordEntry.getValue()));
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
		return (StringUtils.isNotBlank(referenceField) ? faustRegister.resolve(referenceField) : null);
	}
}
