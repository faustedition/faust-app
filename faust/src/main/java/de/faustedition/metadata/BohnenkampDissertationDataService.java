package de.faustedition.metadata;

import static de.faustedition.xml.XPathUtil.xpath;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.Log;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlUtil;

@Service
public class BohnenkampDissertationDataService implements InitializingBean {
	private static final Resource XML_RESOURCE = new ClassPathResource("abr-dissertation-text.xml",
			BohnenkampDissertationDataService.class);
	private static final Resource TEI_TRANSFORMATION_RESOURCE = new ClassPathResource("abr-dissertation-2-tei.xsl",
			BohnenkampDissertationDataService.class);
	private static final Pattern GSA_CALL_NUMBER_PATTERN = Pattern.compile("^GSA ([XVI]+) \\(([0-9]+),([0-9]+)");
	private Document document;
	private Templates teiTransformer;

	public Document getDocument() {
		return document;
	}

	public List<BohnenkampParalipomenonTranscription> extractParalipomena() throws XPathExpressionException {
		List<BohnenkampParalipomenonTranscription> result = new LinkedList<BohnenkampParalipomenonTranscription>();
		for (Element paralipomenonRoot : new NodeListIterable<Element>(xpath("/texte/text", null), document)) {
			Element textElement = XmlUtil.getChild((Element) paralipomenonRoot, "paralipomenon");
			Matcher callNumberMatcher = GSA_CALL_NUMBER_PATTERN.matcher(textElement.getAttribute("n"));
			if (callNumberMatcher.find()) {
				GSACallNumber gsaCallNumber = new GSACallNumber("25/" + callNumberMatcher.group(1) + ","
						+ callNumberMatcher.group(2) + "," + callNumberMatcher.group(3));
				result.add(new BohnenkampParalipomenonTranscription(gsaCallNumber, toTei(textElement),
						toTei(XmlUtil.getChild((Element) paralipomenonRoot, "kommentar"))));
			}
		}
		return result;
	}

	private Element toTei(Element element) {
		try {
			DOMResult result = new DOMResult();
			teiTransformer.newTransformer().transform(new DOMSource(element), result);
			return ((Document) result.getNode()).getDocumentElement();
		} catch (TransformerException e) {
			throw Log.fatalError(e, "Error transforming paralipomenon snippet to TEI");
		}
	}

	public void afterPropertiesSet() throws Exception {
		InputStream dissertationStream = null;
		InputStream teiTransformerStream = null;
		try {
			document = XmlUtil.parse(dissertationStream = XML_RESOURCE.getInputStream());
			teiTransformer = XmlUtil.newTemplates(new StreamSource(teiTransformerStream = TEI_TRANSFORMATION_RESOURCE
					.getInputStream()));
		} finally {
			IOUtils.closeQuietly(teiTransformerStream);
			IOUtils.closeQuietly(dissertationStream);
		}
	}
}
