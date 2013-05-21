package de.faustedition.transcript;

import de.faustedition.FaustURI;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.document.MaterialUnit;
import de.faustedition.transcript.input.FacsimilePathXMLTransformerModule;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import de.faustedition.transcript.input.StageXMLTransformerModule;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.h2.H2TextRepository;
import eu.interedition.text.h2.LayerRelation;
import eu.interedition.text.simple.SimpleLayer;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.CLIXAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.DefaultAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.LineElementXMLTransformerModule;
import eu.interedition.text.xml.module.NotableCharacterXMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.TextXMLTransformerModule;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

@Singleton
public class Transcripts {

	private static final Logger LOG = LoggerFactory.getLogger(Transcripts.class);

    private final DataSource dataSource;
	private final H2TextRepository<JsonNode> textRepository;
	private final XMLStorage xml;
	private final ObjectMapper objectMapper;

    @Inject
    public Transcripts(DataSource dataSource, H2TextRepository<JsonNode> textRepository, XMLStorage xml, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.textRepository = textRepository;
        this.xml = xml;
        this.objectMapper = objectMapper;
    }


    public Layer<JsonNode> textOf(TranscriptRecord transcript) {
        final Long textId = (transcript == null ? null : transcript.getTextId());
        return (textId == null ? null : textRepository.findByIdentifier(textId));

    }

	public TranscriptRecord transcriptOf(final MaterialUnit materialUnit) throws IOException, XMLStreamException {
        final FaustURI source = materialUnit.getTranscriptSource();
        if (source == null) {
            return null;
        }

        return Relations.execute(dataSource, new Relations.Transaction<TranscriptRecord>() {
            @Override
            public TranscriptRecord execute(DSLContext sql) throws Exception {
                TranscriptRecord transcriptRecord = sql.selectFrom(Tables.TRANSCRIPT).fetchOne();
                if (transcriptRecord != null) {
                    return transcriptRecord;
                }

                transcriptRecord = sql.newRecord(Tables.TRANSCRIPT);
                transcriptRecord.setSourceUri(source.toString());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Transforming XML transcript from {}", source);
                }

                final XMLTransformerConfigurationBase<JsonNode> conf = configure(new XMLTransformerConfigurationBase<JsonNode>(textRepository) {

                    @Override
                    protected Layer<JsonNode> translate(Name name, Map<Name, String> attributes, Set<Anchor<JsonNode>> anchors) {
                        return new SimpleLayer<JsonNode>(name, "", objectMapper.valueToTree(attributes), anchors, null);
                    }
                }, materialUnit);

                final StringWriter xmlString = new StringWriter();
                TransformerFactory.newInstance().newTransformer().transform(
                        new SAXSource(xml.getInputSource(source)),
                        new StreamResult(xmlString)
                );

                final Layer<JsonNode> sourceLayer = textRepository.add(TextConstants.XML_TARGET_NAME, new StringReader(xmlString.toString()), null, Collections.<Anchor<JsonNode>>emptySet());
                final LayerRelation<JsonNode> transcriptLayer = (LayerRelation<JsonNode>) new XMLTransformer<JsonNode>(conf).transform(sourceLayer);

                transcriptRecord.setTextId(transcriptLayer.getId());
                transcriptRecord.store();

                TranscribedVerseInterval.register(sql, textRepository, transcriptRecord);

                return transcriptRecord;
            }
        });
    }

	protected XMLTransformerConfigurationBase<JsonNode> configure(XMLTransformerConfigurationBase<JsonNode> conf, MaterialUnit materialUnit) {
		conf.addLineElement(new Name(TEI_NS, "text"));
		conf.addLineElement(new Name(TEI_NS, "div"));
		conf.addLineElement(new Name(TEI_NS, "head"));
		conf.addLineElement(new Name(TEI_NS, "sp"));
		conf.addLineElement(new Name(TEI_NS, "stage"));
		conf.addLineElement(new Name(TEI_NS, "speaker"));
		conf.addLineElement(new Name(TEI_NS, "lg"));
		conf.addLineElement(new Name(TEI_NS, "l"));
		conf.addLineElement(new Name(TEI_NS, "p"));
		conf.addLineElement(new Name(TEI_NS, "ab"));
		conf.addLineElement(new Name(TEI_NS, "line"));
		conf.addLineElement(new Name(TEI_SIG_GE, "document"));

		conf.addContainerElement(new Name(TEI_NS, "text"));
		conf.addContainerElement(new Name(TEI_NS, "div"));
		conf.addContainerElement(new Name(TEI_NS, "lg"));
		conf.addContainerElement(new Name(TEI_NS, "subst"));
		conf.addContainerElement(new Name(TEI_NS, "choice"));
		conf.addContainerElement(new Name(TEI_NS, "zone"));

		conf.exclude(new Name(TEI_NS, "teiHeader"));
		conf.exclude(new Name(TEI_NS, "front"));
		conf.exclude(new Name(TEI_NS, "fw"));
		conf.exclude(new Name(TEI_NS, "app"));

		conf.include(new Name(TEI_NS, "lem"));

		final List<XMLTransformerModule<JsonNode>> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule<JsonNode>());
		modules.add(new NotableCharacterXMLTransformerModule<JsonNode>());
		modules.add(new TextXMLTransformerModule<JsonNode>());
		modules.add(new DefaultAnnotationXMLTransformerModule<JsonNode>());
		modules.add(new CLIXAnnotationXMLTransformerModule<JsonNode>());

		switch (materialUnit.getType()) {
			case ARCHIVALDOCUMENT:
			case DOCUMENT:
				modules.add(new StageXMLTransformerModule(conf));
				break;
			case PAGE:
				modules.add(new HandsXMLTransformerModule(conf));
				modules.add(new FacsimilePathXMLTransformerModule(materialUnit));
				break;
		}

		modules.add(new TEIAwareAnnotationXMLTransformerModule<JsonNode>());

		return conf;
	}
}
