package de.abohnenkamp.paralipomena;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;
import de.swkk.metadata.GSACallNumber;
import de.swkk.metadata.archivedb.ArchiveDatabase;
import de.swkk.metadata.archivedb.ArchiveDatabaseRecord;

public class DissertationText implements InitializingBean {
	private static final Resource XML_RESOURCE = new ClassPathResource("/abr-dissertation-text.xml");
	private static final Resource TEI_TRANSFORMATION_RESOURCE = new ClassPathResource("/tei-transformation.xsl");
	private static final Pattern GSA_CALL_NUMBER_PATTERN = Pattern.compile("^GSA ([XVI]+) \\(([0-9]+),([0-9]+)");
	private Document document;
	private Transformer teiTransformer;

	private ArchiveDatabase archiveDatabase;

	public Document getDocument() {
		return document;
	}

	public List<ParalipomenonTranscription> extractParalipomena() throws XPathException, TransformerException {
		List<ParalipomenonTranscription> result = new LinkedList<ParalipomenonTranscription>();
		XPath xpath = XPathFactory.newInstance().newXPath();

		SortedSet<GSACallNumber> missingCallNumbers = new TreeSet<GSACallNumber>();
		NodeList xpathResult = (NodeList) xpath.evaluate("/texte/text", document, XPathConstants.NODESET);
		for (int nc = 0; nc < xpathResult.getLength(); nc++) {
			Element paralipomenonRoot = (Element) xpathResult.item(nc);
			Element textElement = XMLUtil.firstChildElement(paralipomenonRoot, "paralipomenon");
			Element text = toTei(textElement);
			Element commentary = toTei(XMLUtil.firstChildElement(paralipomenonRoot, "kommentar"));

			String callNumber = textElement.getAttribute("n");
			Matcher callNumberMatcher = GSA_CALL_NUMBER_PATTERN.matcher(callNumber);
			if (callNumberMatcher.find()) {
				GSACallNumber gsaCallNumber = new GSACallNumber("25/" + callNumberMatcher.group(1) + ","  + callNumberMatcher.group(2) + ","+ callNumberMatcher.group(3));
				SortedSet<GSACallNumber> archiveDbRecords = new TreeSet<GSACallNumber>();
				for (ArchiveDatabaseRecord record : archiveDatabase.collect(gsaCallNumber)) {
					archiveDbRecords.add(record.getCallNumber());
				}

				LoggingUtil.LOG.debug(String.format("%s ==> { %s }", gsaCallNumber, StringUtils.join(archiveDbRecords, "; ")));
				if (archiveDbRecords.isEmpty()) {
					missingCallNumbers.add(gsaCallNumber);
				}
			} else {
				System.out.println(callNumber);
			}
		}
		LoggingUtil.LOG.error(String.format("Missing GSA callnumbers: { %s }", StringUtils.join(missingCallNumbers, "; ")));
		return result;
	}

	private Element toTei(Element element) throws TransformerException {
		DOMResult result = new DOMResult();
		teiTransformer.transform(new DOMSource(element), result);
		return ((Document) result.getNode()).getDocumentElement();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		archiveDatabase = new ArchiveDatabase();
		document = XMLUtil.build(XML_RESOURCE.getInputStream());
		teiTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(TEI_TRANSFORMATION_RESOURCE.getInputStream()));
	}
}
