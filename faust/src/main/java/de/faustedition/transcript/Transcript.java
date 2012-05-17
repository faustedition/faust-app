package de.faustedition.transcript;

import com.google.common.io.Closeables;
import de.faustedition.FaustURI;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.util.SimpleXMLTransformerConfiguration;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.*;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.xml.sax.InputSource;

import javax.persistence.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Entity
@Table(name = "faust_transcript")
public class Transcript {

	private long id;
	private String sourceURI;
	private Text text;

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

	@ManyToOne
	@JoinColumn(name = "text_id")
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public static Transcript find(Session session, XMLStorage xml, FaustURI source) throws IOException, XMLStreamException {
		final String sourceURI = source.toString();
		Transcript transcript = (Transcript) session.createCriteria(Transcript.class)
			.add(Restrictions.eq("sourceURI", sourceURI))
			.uniqueResult();
		if (transcript == null) {
			transcript = new Transcript();
			transcript.setSourceURI(sourceURI);
			session.merge(transcript);
		}

		if (transcript.getText() == null) {
			transcript.setText(save(session, xml.getInputSource(source)));
		}

		return transcript;
	}

	private static Text save(Session session, InputSource source) throws XMLStreamException, IOException {
		InputStream xmlStream = null;
		XMLStreamReader xmlReader = null;
		try {
			xmlStream = source.getByteStream();
			xmlReader = XML.createXMLInputFactory().createXMLStreamReader(xmlStream);
			return createXMLTransformer(session).transform(Text.create(session, null, xmlReader));
		} finally {
			XML.closeQuietly(xmlReader);
			Closeables.close(xmlStream, false);
		}
	}

	private static XMLTransformer createXMLTransformer(Session session) {
		final SimpleXMLTransformerConfiguration conf = new SimpleXMLTransformerConfiguration();

		final List<XMLTransformerModule> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule());
		modules.add(new NotableCharacterXMLTransformerModule());
		modules.add(new TextXMLTransformerModule());
		modules.add(new DefaultAnnotationXMLTransformerModule(1000, true));
		modules.add(new CLIXAnnotationXMLTransformerModule(1000));
		modules.add(new TEIAwareAnnotationXMLTransformerModule(1000));

		conf.addLineElement(new Name(TEI_NS, "div"));
		conf.addLineElement(new Name(TEI_NS, "head"));
		conf.addLineElement(new Name(TEI_NS, "sp"));
		conf.addLineElement(new Name(TEI_NS, "stage"));
		conf.addLineElement(new Name(TEI_NS, "speaker"));
		conf.addLineElement(new Name(TEI_NS, "lg"));
		conf.addLineElement(new Name(TEI_NS, "l"));
		conf.addLineElement(new Name(TEI_NS, "p"));
		conf.addLineElement(new Name(TEI_NS, "line"));
		conf.addLineElement(new Name(TEI_SIG_GE, "line"));

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

		return new XMLTransformer(session, conf);
	}

}