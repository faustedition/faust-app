package de.swkk.faustedition;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import de.faustedition.model.HierarchyNode;
import de.faustedition.model.HierarchyNodeType;
import de.faustedition.model.service.HierarchyManager;
import de.faustedition.model.service.MetadataManager;
import de.faustedition.model.xmldb.Collection;
import de.faustedition.model.xmldb.ExistException;
import de.faustedition.util.LoggingUtil;

@Component
public class InventoryDatabase extends AllegroRecordSet {
	private static final long serialVersionUID = -9132830265062249770L;

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
	private MetadataManager metadataManager;

	@Autowired
	private MetadataFieldMapping metadataFieldMapping;

	public void setFaustRegister(WeimarerAusgabeFaustRegister faustRegister) {
		this.faustRegister = faustRegister;
	}

	@Override
	public Resource getRecordSetResource() {
		return new ClassPathResource("/weimar_manuscripts_faust.txt");
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

			String fileGroupName = String.format("GSA %s/%s,%s", callNumber.getPortfolio(), callNumber.getSubPortfolio(),
					callNumber.getFile());
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
					metadataManager.createValue(recordNode, metadataEntry.getKey(), metadataEntry.getValue());
				}

				// archiveDatabase.lookup(callNumber);
			} else {
				LoggingUtil.log(Level.SEVERE, leafCallNumber);
			}

		}
	}

	public void createTranscriptionTemplates() throws IOException, TransformerException, SAXException, ExistException {
		for (AllegroRecord record : this) {
			GSACallNumber callNumber = getCallNumber(record);
			String faustReference = getFaustReference(record);

			Collection faustFolder = Collection.ROOT.createEntry(faustReference == null ? "nicht_zugeordnet" : faustReference);
			Collection portfolioCollection = faustFolder.createEntry("GSA " + callNumber.getFileValue());
			archiveDatabase.addFacsimiles(callNumber, portfolioCollection);

			// if (callNumber.isContent()) {
			// folder = transcriptionStore.findOrCreateFolder(folder,
			// Collection.normalizeName("GSA " + callNumber.toString()));
			// }
			//
			// addMetadata(record, folder);

		}
	}

	public void addMetadata(AllegroRecord record, Collection folder) throws IOException, SAXException, TransformerException {
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

		// for (Map.Entry<String, String> metadataEntry : metadata.entrySet()) {
		// metadataManager.createValue(recordNode,
		// metadataEntry.getKey(), metadataEntry.getValue());
		// }
	}
}
