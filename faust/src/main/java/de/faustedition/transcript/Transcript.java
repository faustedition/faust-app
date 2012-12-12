package de.faustedition.transcript;

import java.io.IOException;
import java.io.InputStream;

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
import de.faustedition.transcript.input.TranscriptInvalidException;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Text;
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
	private Text text;
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
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(getSource()).toString();
	}

	public static Transcript read(Session session, XMLStorage xml, MaterialUnit materialUnit, XMLTransformer transformer)
		throws IOException, XMLStreamException {
		final FaustURI source = materialUnit.getTranscriptSource();
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
			transcript.setMaterialUnitId(materialUnit.node.getId());
			transcript.setSourceURI(sourceURI);
			session.save(transcript);
		}

		if (transcript.getText() == null) {
			transcript.setText(readText(session, xml, source, transformer));
			TranscribedVerseInterval.register(session, transcript);
		}

		return transcript;
	}

	private static Text readText(Session session, XMLStorage xml, FaustURI source, XMLTransformer transformer) 
		throws XMLStreamException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Transforming XML transcript from {}", source);
		}
		InputStream xmlStream = null;
		XMLStreamReader xmlReader = null;
		try {
			xmlStream = xml.getInputSource(source).getByteStream();
			xmlReader = XML.createXMLInputFactory().createXMLStreamReader(xmlStream);
			return transformer.transform(Text.create(session, null, xmlReader));
		} catch(IllegalArgumentException e) {
			throw new TranscriptInvalidException(e);
		} finally {
			XML.closeQuietly(xmlReader);
			Closeables.close(xmlStream, false);
		}
	}
}