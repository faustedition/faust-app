package de.faustedition.transcript;

import java.io.IOException;
import java.io.Reader;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codehaus.jackson.JsonNode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Layer;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLTransformer;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Entity
@Table(name = "faust_transcript")
public class Transcript {
	private static final Logger LOG = LoggerFactory.getLogger(Transcript.class);

	private long id;
	private String sourceURI;
	private Layer text;
	private long materialUnitId;

	@Id
	@GeneratedValue
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "source_uri", nullable = false, unique = true)
	public String getSourceURI() {
		return sourceURI;
	}

	public void setSourceURI(String sourceURI) {
		this.sourceURI = sourceURI;
	}

	@Transient
	public FaustURI getSource() {
		return FaustURI.parse(getSourceURI());
	}

	public void setSource(FaustURI source) {
		setSourceURI(source.toString());
	}

	@Column(name = "material_unit_id", nullable = false)
	public long getMaterialUnitId() {
		return materialUnitId;
	}

	public void setMaterialUnitId(long materialUnitId) {
		this.materialUnitId = materialUnitId;
	}


	@ManyToOne
	@JoinColumn(name = "text_id")
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

	private static Transcript doRead(Session session, XMLStorage xml, FaustURI source, TextRepository<JsonNode> textRepo, XMLTransformer transformer)
	throws IOException, XMLStreamException {
			Preconditions.checkArgument(source != null);

			final String sourceURI = source.toString();
			Transcript transcript = (Transcript) session.createCriteria(Transcript.class)
				.add(Restrictions.eq("sourceURI", sourceURI))
				.setLockMode(LockMode.UPGRADE_NOWAIT)
				.uniqueResult();
			if (transcript == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Creating transcript for {}", sourceURI);
				}
				transcript = new Transcript();
				
				transcript.setSourceURI(sourceURI);

			}

			if (transcript.getText() == null) {

				transcript.setText(readText(session, xml, source, textRepo, transformer));
				TranscribedVerseInterval.register(session, textRepo, transcript);
			}

			return transcript;
	}
	
	public static Transcript read(Session session, XMLStorage xml, MaterialUnit materialUnit, TextRepository textRepo, XMLTransformer transformer)
	throws IOException, XMLStreamException {
		final FaustURI source = materialUnit.getTranscriptSource();

		Transcript transcript = doRead(session, xml, source, textRepo, transformer);
		transcript.setMaterialUnitId(materialUnit.node.getId());
		session.save(transcript);
		return transcript;
	}

	public static Transcript read(Session session, XMLStorage xml, FaustURI source, TextRepository textRepo, XMLTransformer transformer)
	throws IOException, XMLStreamException {
		Transcript transcript = doRead(session, xml, source, textRepo, transformer);
		session.save(transcript);
		return transcript;
	}
	
	
	private static Layer<JsonNode> readText(Session session, XMLStorage xml, FaustURI source, TextRepository<JsonNode> textRepo, XMLTransformer transformer) 
		throws XMLStreamException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Transforming XML transcript from {}", source);
		}
		Reader xmlStream = null;
		XMLStreamReader xmlReader = null;
		try {
			xmlStream = xml.getInputSource(source).getCharacterStream();
			//xmlReader = XML.createXMLInputFactory().createXMLStreamReader(xmlStream);
			return transformer.transform(textRepo.add(TextConstants.XML_TARGET_NAME, xmlStream, null));
		} finally {
			XML.closeQuietly(xmlReader);
			Closeables.close(xmlStream, false);
		}
	}
}