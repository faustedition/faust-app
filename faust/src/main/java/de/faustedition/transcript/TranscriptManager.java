package de.faustedition.transcript;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Closeables;
import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.transcript.input.FacsimilePathXMLTransformerModule;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import de.faustedition.transcript.input.StageXMLTransformerModule;
import de.faustedition.transcript.input.TranscriptInvalidException;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.neo4j.LayerNode;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import eu.interedition.text.simple.SimpleLayer;
import eu.interedition.text.xml.XML;
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
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;
import static org.neo4j.graphdb.Direction.INCOMING;

@Component
public class TranscriptManager implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(TranscriptManager.class);

  public static final FaustRelationshipType TRANSCRIPT_RT = new FaustRelationshipType("transcribes");

  @Autowired
  private Neo4jTextRepository<JsonNode> textRepository;

  @Autowired
  private FaustGraph faustGraph;

  @Autowired
  private ObjectMapper objectMapper;

  private TreeMultimap<Short, MaterialUnit> transcribedVerseIndex = TreeMultimap.create(Ordering.natural(), new Comparator<MaterialUnit>() {
    @Override
    public int compare(MaterialUnit o1, MaterialUnit o2) {
      final long idDiff = o1.node.getId() - o2.node.getId();
      return (idDiff == 0 ? 0 : (idDiff < 0 ? -1 : 1));
    }
  });

  public Layer<JsonNode> find(MaterialUnit materialUnit) {
    final Relationship rel = materialUnit.node.getSingleRelationship(TRANSCRIPT_RT, INCOMING);
    return (rel == null ? null : new LayerNode<JsonNode>(textRepository, rel.getStartNode()));
  }

  public void read(XMLStorage xml, MaterialUnit materialUnit) throws IOException, XMLStreamException {
    final XMLTransformerConfigurationBase<JsonNode> conf = configure(new XMLTransformerConfigurationBase<JsonNode>(textRepository) {

      @Override
      protected Layer<JsonNode> translate(Name name, Map<Name, Object> attributes, Set<Anchor<JsonNode>> anchors) {
        return new SimpleLayer<JsonNode>(name, "", objectMapper.valueToTree(attributes), anchors);
      }
    }, materialUnit);

    final FaustURI source = materialUnit.getTranscriptSource();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Transforming XML transcript from {}", source);
    }

    try {
      final StringWriter xmlString = new StringWriter();
      TransformerFactory.newInstance().newTransformer().transform(
              new SAXSource(xml.getInputSource(source)),
              new StreamResult(xmlString)
      );
      final Layer<JsonNode> sourceLayer = textRepository.add(TextConstants.XML_TARGET_NAME, new StringReader(xmlString.toString()), null);
      final LayerNode<JsonNode> transcriptLayer = (LayerNode<JsonNode>) new XMLTransformer<JsonNode>(conf).transform(sourceLayer);
      transcriptLayer.node.createRelationshipTo(materialUnit.node, TRANSCRIPT_RT);
    } catch (IllegalArgumentException e) {
      throw new TranscriptInvalidException(e);
    } catch (TransformerException e) {
      throw new TranscriptInvalidException(e);
    }
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

  @Override
  public void afterPropertiesSet() throws Exception {
    Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
    });
  }
}
