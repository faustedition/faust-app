package de.abohnenkamp.paralipomena;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.practicalxml.DomUtil;
import net.sf.practicalxml.ParseUtil;
import net.sf.practicalxml.xpath.XPathWrapper;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import de.faustedition.util.ErrorUtil;
import de.swkk.metadata.GSACallNumber;

public class DissertationText implements InitializingBean
{
	private static final Resource XML_RESOURCE = new ClassPathResource("/data/abr-dissertation-text.xml");
	private static final Resource TEI_TRANSFORMATION_RESOURCE = new ClassPathResource("/xsl/abr-dissertation-tei-transformation.xsl");
	private static final Pattern GSA_CALL_NUMBER_PATTERN = Pattern.compile("^GSA ([XVI]+) \\(([0-9]+),([0-9]+)");
	private Document document;
	private Transformer teiTransformer;

	public Document getDocument()
	{
		return document;
	}

	public List<ParalipomenonTranscription> extractParalipomena()
	{
		List<ParalipomenonTranscription> result = new LinkedList<ParalipomenonTranscription>();
		for (Node paralipomenonRoot : new XPathWrapper("/texte/text").evaluate(document))
		{
			Element textElement = DomUtil.getChild((Element) paralipomenonRoot, "paralipomenon");
			Matcher callNumberMatcher = GSA_CALL_NUMBER_PATTERN.matcher(textElement.getAttribute("n"));
			if (callNumberMatcher.find())
			{
				GSACallNumber gsaCallNumber = new GSACallNumber("25/" + callNumberMatcher.group(1) + "," + callNumberMatcher.group(2) + "," + callNumberMatcher.group(3));
				result.add(new ParalipomenonTranscription(gsaCallNumber, toTei(textElement), toTei(DomUtil.getChild((Element) paralipomenonRoot, "kommentar"))));
			}
		}
		return result;
	}

	private Element toTei(Element element)
	{
		try
		{
			DOMResult result = new DOMResult();
			teiTransformer.transform(new DOMSource(element), result);
			return ((Document) result.getNode()).getDocumentElement();
		} catch (TransformerException e)
		{
			throw ErrorUtil.fatal(e, "Error transforming paralipomenon snippet to TEI");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		document = ParseUtil.parse(new InputSource(XML_RESOURCE.getInputStream()));
		teiTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(TEI_TRANSFORMATION_RESOURCE.getInputStream()));
	}
}
