package de.faustedition.transcript;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TranscriptSourceResource extends TranscriptResource {

	private Transcript transcript;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		try {
			this.transcript = Transcript.find(sessionFactory.getCurrentSession(), xml, document.getTranscriptSource());
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
}
