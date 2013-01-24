package de.faustedition.transcript;

import static eu.interedition.text.Query.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.codehaus.jackson.JsonNode;
import org.hibernate.Session;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.JsonRepresentationFactory;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TranscriptSourceResource extends TranscriptResource {

	@Autowired
	private JsonRepresentationFactory jsonFactory;
	
	@Autowired
	private TextualTranscripts textualTranscripts;

	@Autowired
	private TextRepository<JsonNode> textRepo;
	
	private Transcript transcript;
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		try {
			this.transcript = textualTranscripts.read(sessionFactory.getCurrentSession(), xml, materialUnit);
		} catch (XMLStreamException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}

	}

	@Get("xml")
	public Representation source() throws IOException, XMLStreamException, SAXException {
		Text xmlSourceText = transcript.getText().getAnchors().iterator().next().getText();
		//FIXME How to do this properly?
		return new StringRepresentation(xmlSourceText.read());
	}

	@Get("txt")
	public Representation plainText() throws IOException {
		return new StringRepresentation(transcript.getText().read());
	}

	@Get("json")
	public Representation model() throws IOException {
		final Session session = sessionFactory.getCurrentSession();
		final Text text = transcript.getText();

		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();
		for (Layer<JsonNode> annotation : textRepo.query(text(text))) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.hashCode()), name);
			annotations.add(annotation);
		}
		return jsonFactory.map(new ModelMap()
			.addAttribute("text", text)
			.addAttribute("textContent", text.read())
			.addAttribute("names", names)
			.addAttribute("annotations", annotations));
	}
}
