package de.faustedition.document;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import de.faustedition.FaustURI;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.db.tables.records.MaterialUnitRecord;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLBaseTracker;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class DocumentDescriptorParser extends DefaultHandler {

	private static final Logger LOG = Logger.getLogger(DocumentDescriptorParser.class.getName());

    private static final Set<String> MATERIAL_UNIT_NAMES = ImmutableSet.of(
            "archivalDocument",
            "sheet",
            "leaf",
            "disjunctLeaf",
            "page",
            "patch",
            "patchSurface"
    );

    private final DSLContext sql;
    private final ObjectMapper objectMapper;
    private final Map<String, Long> archiveIds;
    private final FaustURI source;
	private final XMLBaseTracker baseTracker;

    private Deque<ObjectNode> unitStack = new ArrayDeque<ObjectNode>();

	private String currentKey;
	private StringBuilder currentValue;

    private DocumentRecord document;
    private int materialUnitCounter;
    private boolean inMetadataSection;

    public DocumentDescriptorParser(DSLContext sql, ObjectMapper objectMapper, Map<String, Long> archiveIds, FaustURI source) {
        this.sql = sql;
        this.objectMapper = objectMapper;
        this.archiveIds = archiveIds;
        this.source = source;
        this.baseTracker = new XMLBaseTracker(source.toString());
    }

	@Override
	public void startDocument() throws SAXException {
		materialUnitCounter = 0;
		inMetadataSection = false;
        document = null;
		currentKey = null;
		currentValue = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		baseTracker.startElement(uri, localName, qName, attributes);
		if (!Namespaces.FAUST_NS_URI.equals(uri)) {
			return;
		}

		if (MATERIAL_UNIT_NAMES.contains(localName)) {
            final ObjectNode unitData = objectMapper.createObjectNode();
            unitData.put("type", localName);
            unitData.put("order", materialUnitCounter++);

            if (unitStack.isEmpty()) {
                document = sql.newRecord(Tables.DOCUMENT);
                document.setDescriptorUri(source.toString());
                document.store();
            } else {
                final ObjectNode parent = unitStack.peek();
                final ArrayNode contents = parent.has("contents")
                        ? (ArrayNode) parent.get("contents")
                        : parent.putArray("contents");
                contents.add(unitData);
            }
            unitStack.push(unitData);
		} else if ("metadata".equals(localName) && !unitStack.isEmpty()) {
			inMetadataSection = true;
		} else if (inMetadataSection && currentKey == null) {
            if (localName.equals("textTranscript") || localName.equals("docTranscript")) {
                final String transcript = Strings.nullToEmpty(attributes.getValue("uri")).trim();
                if (!transcript.isEmpty()) {
                    unitStack.peek().put("transcriptSource", new FaustURI(baseTracker.getBaseURI().resolve(transcript)).toString());
                }
            } else {
                currentKey = toKey(localName, attributes);
                currentValue = new StringBuilder();
            }
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		baseTracker.endElement(uri, localName, qName);
		if (!Namespaces.FAUST_NS_URI.equals(uri)) {
			return;
		}

		if (MATERIAL_UNIT_NAMES.contains(localName)) {
            try {
                final ObjectNode unitData = unitStack.pop();
                if (unitStack.isEmpty()) {
                    final String archiveId = unitData.path("archive").asText();
                    if (!archiveId.isEmpty()) {
                        document.setArchiveId(Preconditions.checkNotNull(archiveIds.get(archiveId), archiveId));
                    }
                    final String callNumberPrefix = "callnumber" + (archiveId.isEmpty() ? "" : "." + archiveId);
                    final String waIdPrefix = "callnumber.wa-";

                    String callnumber = null;
                    String waId = null;
                    for (Iterator<Map.Entry<String,JsonNode>> it = unitData.getFields(); it.hasNext(); ) {
                        final Map.Entry<String, JsonNode> metadata = it.next();
                        final String metadataKey = metadata.getKey();
                        if (metadataKey.startsWith(callNumberPrefix)) {
                            callnumber = Strings.emptyToNull(metadata.getValue().asText());
                        } else if (metadataKey.startsWith(waIdPrefix)) {
                            waId = Strings.emptyToNull(metadata.getValue().asText());
                        }
                    }

                    document.setMetadata(objectMapper.writeValueAsString(unitData));
                    document.setCallnumber(callnumber);
                    document.setWaId(waId);
                    document.store();
                }

                Long transcriptId = null;
                if (unitData.has("transcriptSource")) {
                    final String transcriptSource = unitData.remove("transcriptSource").asText();
                    final Record1<Long> transcript = sql.select(Tables.TRANSCRIPT.ID).from(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.SOURCE_URI.eq(transcriptSource)).fetchOne();
                    if (transcript == null) {
                        final TranscriptRecord transcriptRecord = sql.newRecord(Tables.TRANSCRIPT);
                        transcriptRecord.setSourceUri(transcriptSource);
                        transcriptRecord.store();
                        transcriptId = transcriptRecord.getId();
                    } else {
                        transcriptId = transcript.getValue(Tables.TRANSCRIPT.ID);
                    }
                }
                final MaterialUnitRecord unit = sql.newRecord(Tables.MATERIAL_UNIT);
                unit.setDocumentId(document.getId());
                unit.setDocumentOrder(unitData.path("order").asInt());
                unit.setTranscriptId(transcriptId);
                unit.store();
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        } else if (inMetadataSection && "metadata".equals(localName)) {
			inMetadataSection = false;
		} else if (inMetadataSection && currentKey != null) {
            final String value = currentValue.toString().trim().replaceAll("\\s+", " ");
            if (!value.isEmpty()) {
                final ObjectNode unitData = unitStack.peek();
                if (!unitData.has(currentKey)) {
                    unitData.put(currentKey, value);
                } else {
                    final JsonNode currentValue = unitData.get(currentKey);
                    if (currentValue.isArray()) {
                        ((ArrayNode) currentValue).add(value);
                    } else {
                        final ArrayNode metadataValues = objectMapper.createArrayNode();
                        metadataValues.add(unitData.get(currentKey));
                        metadataValues.add(value);
                        unitData.put(currentKey, metadataValues);
                    }
                }
            }
			currentKey = null;
			currentValue = null;
		}
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inMetadataSection && currentKey != null) {
			currentValue.append(ch, start, length);
		}
	}

    protected String toKey(String localName, Attributes attributes) {
        String key = localName;
        if ("idno".equals(key)) {
            final String type = Strings.nullToEmpty(attributes.getValue("", "type")).trim();
            key = ("callnumber" + (type.isEmpty() ? "" : "." + type));
        } else if ("repository".equals(key)) {
            key = "archive";
        }

        final StringBuilder converted = new StringBuilder();
        for (int cc = 0; cc < key.length(); cc++) {
            char current = key.charAt(cc);
            if (cc > 0 && Character.isUpperCase(current)) {
                converted.append("-");
                current = Character.toLowerCase(current);
            }
            if (!Character.isLetterOrDigit(current) && current != '.') {
                current = '-';
            }
            converted.append(current);
        }
        return converted.toString();
    }
}
