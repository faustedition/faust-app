package de.faustedition.transcript;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import de.faustedition.FaustURI;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.transcript.input.TranscriptInvalidException;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLTransformer;
import org.codehaus.jackson.JsonNode;
import org.jooq.DSLContext;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Transcript extends NodeWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(Transcript.class);

    private long id;
    private String sourceURI;
    private Layer<JsonNode> text;
    private long materialUnitId;

    public Transcript(Node node) {
        super(node);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public FaustURI getSource() {
        return FaustURI.parse(getSourceURI());
    }

    public void setSource(FaustURI source) {
        setSourceURI(source.toString());
    }

    public long getMaterialUnitId() {
        return materialUnitId;
    }

    public void setMaterialUnitId(long materialUnitId) {
        this.materialUnitId = materialUnitId;
    }

    public Layer<JsonNode> getText() {
        return text;
    }

    public void setText(Layer<JsonNode> text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(getSource()).toString();
    }

    private static TranscriptRecord doRead(final DataSource dataSource, final XMLStorage xml, final MaterialUnit materialUnit, final FaustURI source, final TextRepository<JsonNode> textRepo, final XMLTransformer<JsonNode> transformer) throws IOException, XMLStreamException {
        Preconditions.checkArgument(source != null);
        return Relations.execute(dataSource, new Relations.Transaction<TranscriptRecord>() {
            @Override
            public TranscriptRecord execute(DSLContext sql) throws Exception {
                final String sourceURI = source.toString();
                TranscriptRecord transcriptRecord = sql.selectFrom(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.SOURCE_URI.eq(sourceURI)).fetchOne();
                if (transcriptRecord == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Creating transcript for {}", sourceURI);
                    }

                    transcriptRecord = new TranscriptRecord();
                    transcriptRecord.setSourceUri(sourceURI);
                    transcriptRecord.setMaterialUnitId(materialUnit == null ? null : materialUnit.node.getId());
                }
                Layer<JsonNode> text = null;
                if (transcriptRecord.getTextId() == null) {
                    text = readText(xml, source, textRepo, transformer);
                    transcriptRecord.setTextId(text.getId());
                }
                transcriptRecord.store();

                if (text != null) {
                    // the text of the transcript has been read, register its verse intervals
                    TranscribedVerseInterval.register(sql, textRepo, transcriptRecord);
                }

                return transcriptRecord;
            }
        });
    }

    public static TranscriptRecord read(DataSource dataSource, XMLStorage xml, MaterialUnit materialUnit, TextRepository<JsonNode> textRepo, XMLTransformer<JsonNode> transformer)
            throws IOException, XMLStreamException {
        return doRead(dataSource, xml, materialUnit, materialUnit.getTranscriptSource(), textRepo, transformer);
    }

    public static TranscriptRecord read(DataSource dataSource, XMLStorage xml, FaustURI source, TextRepository<JsonNode> textRepo, XMLTransformer<JsonNode> transformer)
            throws IOException, XMLStreamException {
        return doRead(dataSource, xml, null, source, textRepo, transformer);
    }


    private static Layer<JsonNode> readText(XMLStorage xml, FaustURI source, TextRepository<JsonNode> textRepo, XMLTransformer<JsonNode> transformer)
            throws XMLStreamException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Transforming XML transcript from {}", source);
        }
        Reader xmlStream = null;
        XMLStreamReader xmlReader = null;
        try {
            xmlStream = xml.getInputSource(source).getCharacterStream();
            //xmlReader = XML.createXMLInputFactory().createXMLStreamReader(xmlStream);
            return transformer.transform(textRepo.add(TextConstants.XML_TARGET_NAME, xmlStream, null, Collections.<Anchor<JsonNode>>emptySet()));
        } catch (IllegalArgumentException e) {
            throw new TranscriptInvalidException(e);
        } finally {
            XML.closeQuietly(xmlReader);
            Closeables.close(xmlStream, false);
        }
    }
}