package de.swkk.metadata;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.Folder;
import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.service.HierarchyManager;
import de.faustedition.util.LoggingUtil;

public class InventoryDatabase extends AllegroRecordSet {
	private static final String FAUST_REFERENCE_FIELD = "11z";

	@Autowired
	private ArchiveDatabase archiveDatabase;

	@Autowired
	private CategoryResolver categoryResolver;

	@Autowired
	private WeimarerAusgabeFaustRegister faustRegister;

	@Autowired
	private HierarchyManager hierarchyManager;

	@Autowired
	private MetadataFieldMapping metadataFieldMapping;

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

	public void createMetadataStructure() {
		for (AllegroRecord record : this) {
			GSACallNumber callNumber = getCallNumber(record);

			HierarchyNode registerNode = hierarchyManager.loadNode(faustRegister.resolveMetadataNode(getFaustReference(record)));

			String fileGroupName = String.format("GSA %s/%s,%s", callNumber.getPortfolio(), callNumber.getSubPortfolio(), callNumber.getFile());
			HierarchyNode callNumberGroupNode = hierarchyManager.findNode(registerNode, fileGroupName);
			if (callNumberGroupNode == null) {
				callNumberGroupNode = hierarchyManager.createNode(registerNode, fileGroupName, HierarchyNodeType.FILE);
				callNumberGroupNode.getFullPath();
			}

			String leafCallNumber = "GSA " + callNumber.toString();
			if (!hierarchyManager.nodeExists(callNumberGroupNode, leafCallNumber)) {
				HierarchyNode recordNode = hierarchyManager.createNode(callNumberGroupNode, leafCallNumber, HierarchyNodeType.FILE);

				Map<String, String> metadata = new HashMap<String, String>();
				for (Map.Entry<String, String> recordField : record.entrySet()) {
					if (metadataFieldMapping.containsKey(recordField.getKey())) {
						String metadataField = metadataFieldMapping.get(recordField.getKey());
						if (metadata.containsKey(metadataField)) {
							metadata.put(metadataField, metadata.get(metadataField) + "\n" + recordField.getValue());
						} else {
							metadata.put(metadataField, recordField.getValue());
						}
					}
				}

				for (Map.Entry<String, String> metadataEntry : metadata.entrySet()) {
					//metadataManager.createValue(recordNode, metadataEntry.getKey(), metadataEntry.getValue());
				}

				// archiveDatabase.lookup(callNumber);
			} else {
				LoggingUtil.log(Level.SEVERE, leafCallNumber);
			}

		}
	}

	public void createTranscriptionTemplates() throws IOException, DocumentException {
		for (AllegroRecord record : this) {
			GSACallNumber callNumber = getCallNumber(record);

			String faustReference = getFaustReference(record);
			//Folder parentFolder = transcriptionManager.findFolder(null, Folder.normalizeName(faustReference == null ? "nicht zugeordnet" : faustReference));

			//Folder folder = transcriptionManager.findOrCreateFolder(parentFolder, Folder.normalizeName("GSA " + callNumber.getFileValue()));
			
			if (!callNumber.isContent()) {
				//archiveDatabase.addFacsimiles(callNumber, folder);
			}

			if (callNumber.isContent()) {
				//folder = transcriptionManager.findOrCreateFolder(folder, Folder.normalizeName("GSA " + callNumber.toString()));
			}

			//addMetadata(record, folder);

		}
	}

	public void addMetadata(AllegroRecord record, Folder folder) throws IOException, DocumentException {
		//MetadataDocument metadataDocument = transcriptionManager.getMetadataDocument(folder);

		Map<String, String> metadata = new HashMap<String, String>();
		for (Map.Entry<String, String> recordField : record.entrySet()) {
			if (metadataFieldMapping.containsKey(recordField.getKey())) {
				String metadataField = metadataFieldMapping.get(recordField.getKey());
				if (metadata.containsKey(metadataField)) {
					metadata.put(metadataField, metadata.get(metadataField) + "\n" + recordField.getValue());
				} else {
					metadata.put(metadataField, recordField.getValue());
				}
			}
		}

		for (Map.Entry<String, String> metadataEntry : metadata.entrySet()) {
			// metadataManager.createValue(recordNode,
			// metadataEntry.getKey(), metadataEntry.getValue());
		}

		//metadataDocument.save();
	}
}
