package de.faustedition.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import de.faustedition.db.tables.records.MaterialUnitRecord;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLBaseTracker;
import de.faustedition.xml.XMLUtil;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class DocumentDescriptorParser extends DefaultHandler {

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
    private final FaustURI source;
    private final ObjectMapper objectMapper;
    private final Sources xml;
    private final Map<String, Long> archives;

    private final XMLBaseTracker baseTracker;
    private final Deque<ObjectNode> unitStack = new ArrayDeque<ObjectNode>();

    private String currentKey;
    private StringBuilder currentValue;

    private DocumentRecord document;
    private int materialUnitCounter;
    private boolean inMetadataSection;

    DocumentDescriptorParser(DSLContext sql, FaustURI source, ObjectMapper objectMapper, Sources xml, Map<String, Long> archives) {
        this.sql = sql;
        this.source = source;
        this.objectMapper = objectMapper;
        this.xml = xml;
        this.archives = archives;
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
                document.setLastRead(new Timestamp(System.currentTimeMillis()));
                document.setDescriptorUri(Documents.uri2Path(source));
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
                    try {
                        unitStack.peek().put("transcriptSource", new FaustURI(baseTracker.getBaseURI().resolve(transcript)).toString());
                    } catch (IllegalArgumentException e) {
                        throw new SAXException(transcript, e);
                    }
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
            final ObjectNode unitData = unitStack.pop();
            if (unitStack.isEmpty()) {
                final String archiveId = unitData.path("archive").asText();
                if (!archiveId.isEmpty()) {
                    document.setArchiveId(Preconditions.checkNotNull(archives.get(archiveId), archiveId));
                }
                final String callNumberPrefix = "callnumber" + (archiveId.isEmpty() ? "" : "." + archiveId);
                final String waIdPrefix = "callnumber.wa_";

                String callnumber = null;
                String waId = null;
                for (Iterator<Map.Entry<String, JsonNode>> it = unitData.fields(); it.hasNext(); ) {
                    final Map.Entry<String, JsonNode> metadata = it.next();
                    final String metadataKey = metadata.getKey();
                    if (metadataKey.startsWith(callNumberPrefix)) {
                        callnumber = Strings.emptyToNull(metadata.getValue().asText());
                    } else if (metadataKey.startsWith(waIdPrefix)) {
                        waId = Strings.emptyToNull(metadata.getValue().asText());
                    }
                }
                document.setCallnumber(callnumber);
                document.setWaId(waId);

                try {
                    document.setMetadata(objectMapper.writeValueAsString(unitData));
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }

                document.store();
            }

            final MaterialUnitRecord unit = sql.newRecord(Tables.MATERIAL_UNIT);
            unit.setDocumentId(document.getId());
            unit.setDocumentOrder(unitData.path("order").asInt());
            unit.store();

            final List<String> facsimiles = Lists.newLinkedList();
            String textImageLinkUri = null;
            if (unitData.has("transcriptSource")) {
                final String transcriptSource = Documents.uri2Path(new FaustURI(URI.create(unitData.remove("transcriptSource").asText())));
                final Record1<Long> transcript = sql.select(Tables.TRANSCRIPT.ID).from(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.SOURCE_URI.eq(transcriptSource)).fetchOne();
                if (transcript == null) {
                    try {
                        final FacsimileReferenceParser facsimileReferenceParser = new FacsimileReferenceParser(source);
                        XMLUtil.saxParser().parse(
                                xml.getInputSource(new FaustURI(FaustAuthority.XML, "/" + transcriptSource)),
                                facsimileReferenceParser
                        );
                        facsimiles.addAll(facsimileReferenceParser.getFacsimileReferences());
                        textImageLinkUri = facsimileReferenceParser.getTextImageLinkReference();
                    } catch (IOException e) {
                        LOG.log(Level.WARNING, "I/O error while extracting facsimile references from " + transcriptSource, e);
                    } catch (SAXException e) {
                        LOG.log(Level.WARNING, "XML error while extracting facsimile references from " + transcriptSource, e);
                    }

                    final TranscriptRecord transcriptRecord = sql.newRecord(Tables.TRANSCRIPT);
                    transcriptRecord.setSourceUri(transcriptSource);
                    transcriptRecord.setMaterialUnitId(unit.getId());
                    transcriptRecord.setTextImageLinkUri(textImageLinkUri);
                    transcriptRecord.store();

                    final long transcriptId = transcriptRecord.getId();
                    int facsimileOrder = 0;
                    for (String facsimile : facsimiles) {
                        sql.insertInto(
                                Tables.FACSIMILE,
                                Tables.FACSIMILE.TRANSCRIPT_ID,
                                Tables.FACSIMILE.FACSIMILE_ORDER,
                                Tables.FACSIMILE.PATH
                        ).values(
                                transcriptId,
                                facsimileOrder++,
                                facsimile
                        ).execute();
                    }
                }
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
            return ("callnumber" + (type.isEmpty() ? "" : "." + type));
        }

        if ("repository".equals(key)) {
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
