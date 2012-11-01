package de.faustedition.transcript;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import org.hibernate.Session;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

import static eu.interedition.text.query.QueryCriteria.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TranscriptSourceResource extends TranscriptResource {

	@Autowired
	private JsonRepresentationFactory jsonFactory;

	private Transcript transcript;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		try {
			this.transcript = TextualTranscripts.read(sessionFactory.getCurrentSession(), xml, materialUnit);
		} catch (XMLStreamException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

	}

	@Get("xml")
	public Representation source() throws IOException, XMLStreamException, SAXException {
		final Reader xmlReader = transcript.getText().getLayer().getTarget().getText().read().getInput();
		try {
			final org.w3c.dom.Document xml = XMLUtil.documentBuilder().parse(new InputSource(xmlReader));
			for (Element handNote : new NodeListWrapper<Element>(XPathUtil.xpath("//tei:handNotes"), xml)) {
				handNote.getParentNode().removeChild(handNote);
			}
			for (Element handNote : new NodeListWrapper<Element>(XPathUtil.xpath("//tei:charDecl"), xml)) {
				handNote.getParentNode().removeChild(handNote);
			}
			return new DomRepresentation(MediaType.APPLICATION_XML, xml);
		} finally {
			Closeables.close(xmlReader, false);
		}
	}

	@Get("txt")
	public Representation plainText() throws IOException {
		return new StringRepresentation(CharStreams.toString(transcript.getText().read()));
	}

	@Get("json")
	public Representation model() throws IOException {
		final Session session = sessionFactory.getCurrentSession();
		final Text text = transcript.getText();

		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Annotation> annotations = Lists.newArrayList();
		for (Annotation annotation : text(text).iterate(session)) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.getId()), name);
			annotations.add(annotation);
		}
		return jsonFactory.map(new ModelMap()
			.addAttribute("text", text)
			.addAttribute("textContent", CharStreams.toString(text.read()))
			.addAttribute("names", names)
			.addAttribute("annotations", annotations));
	}
}
