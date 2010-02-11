package de.abohnenkamp.paralipomena;

import static de.faustedition.model.xml.XPathUtil.xpath;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.model.xml.NodeListIterable;
import de.faustedition.model.xml.XmlUtil;
import de.faustedition.util.ErrorUtil;
import de.swkk.metadata.GSACallNumber;

public class DissertationText implements InitializingBean {
	private static final Resource XML_RESOURCE = new ClassPathResource("/data/abr-dissertation-text.xml");
	private static final Resource TEI_TRANSFORMATION_RESOURCE = new ClassPathResource(
			"/xsl/abr-dissertation-tei-transformation.xsl");
	private static final Pattern GSA_CALL_NUMBER_PATTERN = Pattern.compile("^GSA ([XVI]+) \\(([0-9]+),([0-9]+)");
	private Document document;
	private Transformer teiTransformer;

	public Document getDocument() {
		return document;
	}

	public List<ParalipomenonTranscription> extractParalipomena() throws XPathExpressionException {
		List<ParalipomenonTranscription> result = new LinkedList<ParalipomenonTranscription>();
		for (Element paralipomenonRoot : new NodeListIterable<Element>(xpath("/texte/text", null), document)) {
			Element textElement = XmlUtil.getChild((Element) paralipomenonRoot, "paralipomenon");
			Matcher callNumberMatcher = GSA_CALL_NUMBER_PATTERN.matcher(textElement.getAttribute("n"));
			if (callNumberMatcher.find()) {
				GSACallNumber gsaCallNumber = new GSACallNumber("25/" + callNumberMatcher.group(1) + ","
						+ callNumberMatcher.group(2) + "," + callNumberMatcher.group(3));
				result.add(new ParalipomenonTranscription(gsaCallNumber, toTei(textElement), toTei(XmlUtil
						.getChild((Element) paralipomenonRoot, "kommentar"))));
			}
		}
		return result;
	}

	private Element toTei(Element element) {
		try {
			DOMResult result = new DOMResult();
			teiTransformer.transform(new DOMSource(element), result);
			return ((Document) result.getNode()).getDocumentElement();
		} catch (TransformerException e) {
			throw ErrorUtil.fatal(e, "Error transforming paralipomenon snippet to TEI");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		document = XmlUtil.parse(XML_RESOURCE.getInputStream());
		teiTransformer = XmlUtil.newTransformer(new StreamSource(TEI_TRANSFORMATION_RESOURCE.getInputStream()));
	}
}
