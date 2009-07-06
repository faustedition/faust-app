package de.faustedition.model.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

import de.faustedition.model.TEIDocument;
import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLNamespaceCache;
import de.faustedition.util.XMLUtil;

public class TEIDocumentManagerImpl implements TEIDocumentManager, InitializingBean {
	private static final Resource TEI_TEMPLATE_RESOURCE = new ClassPathResource("/tei-document-template.xml");
	private static final Resource TEI_SERIALIZATION_RESOURCE = new ClassPathResource("/tei-document-serialization.xsl");

	private String cssResourceURL;
	private String schemaResourceURL;

	private Element headerTemplate;
	private Element textTemplate;
	private Templates serializationTemplates;
	private XMLNamespaceCache namespaceCache;

	@Required
	public void setSchemaResourceURL(String schemaResourceURL) {
		this.schemaResourceURL = schemaResourceURL;
	}

	@Required
	public void setCssResourceURL(String cssResourceURL) {
		this.cssResourceURL = cssResourceURL;
	}

	public TEIDocument createDocument() {
		return createTemplateDocument();
	}

	public void configure(TEIDocument teiDocument) throws SAXException, IOException {
		StringBuilder stylesheetPIdata = new StringBuilder();
		stylesheetPIdata.append("href=\"" + cssResourceURL + "\"");
		stylesheetPIdata.append(" type=\"text/css\"");
		XMLUtil.addProcessingInstruction(teiDocument.getDocument(), "xml-stylesheet", stylesheetPIdata.toString());

		StringBuilder schemaPIdata = new StringBuilder();
		schemaPIdata.append("RNGSchema=\"" + schemaResourceURL + "\" ");
		schemaPIdata.append("type=\"compact\"");

		XMLUtil.addProcessingInstruction(teiDocument.getDocument(), "oxygen", schemaPIdata.toString());

		Document document = teiDocument.getDocument();
		Element teiElement = document.getDocumentElement();

		if (teiDocument.getText() == null) {
			teiElement.appendChild(document.importNode(textTemplate, true));
		}

		if (teiDocument.getHeader() == null) {
			teiElement.insertBefore(document.importNode(headerTemplate, true), teiDocument.getText());
		}
	}

	public void serialize(TEIDocument document, OutputStream outStream) throws IOException, TransformerException {
		serializationTemplates.newTransformer().transform(new DOMSource(document.getDocument()), new StreamResult(outStream));
	}

	public void setTitle(TEIDocument teiDocument, String title) throws SAXException, IOException {
		if (teiDocument.getHeader() == null) {
			configure(teiDocument);
		}

		try {
			queryForElement(teiDocument, "/:TEI/:teiHeader/:fileDesc/:titleStmt/:title").setTextContent(title);
		} catch (XPathExpressionException e) {
			throw ErrorUtil.fatal("XPath error while setting TEI title", e);
		}
	}

	public void setManuscriptIdentifier(TEIDocument teiDocument, ManuscriptIdentifier msIdentifier) {
		try {
			Element msIdentifierElement = queryForElement(teiDocument,
					"/:TEI/:teiHeader/:fileDesc/:sourceDesc/:msDesc/:msIdentifier");
			
			Node child = null;
			while ((child = msIdentifierElement.getFirstChild()) != null) {
				msIdentifierElement.removeChild(child);
			}

			XMLUtil.addTextElement(msIdentifierElement, "institution", msIdentifier.getInstitution());
			XMLUtil.addTextElement(msIdentifierElement, "repository", msIdentifier.getRepository());
			XMLUtil.addTextElement(msIdentifierElement, "collection", msIdentifier.getCollection());

			for (Map.Entry<String, String> identifier : msIdentifier.getIdentifiers().entrySet()) {
				Element idElement = XMLUtil.addTextElement(msIdentifierElement, "idno", identifier.getValue());
				idElement.setAttribute("type", identifier.getKey());
			}
		} catch (XPathExpressionException e) {
			throw ErrorUtil.fatal("XPath error while setting manuscript identifier", e);
		}
	}

	public NodeList query(TEIDocument document, String xpath) throws XPathExpressionException {
		XPath xpathObj = XPathFactory.newInstance().newXPath();
		xpathObj.setNamespaceContext(namespaceCache);
		return (NodeList) xpathObj.evaluate(xpath, document.getDocument(), XPathConstants.NODESET);
	}

	public Element queryForElement(TEIDocument document, String xpath) throws XPathExpressionException {
		NodeList elementList = query(document, xpath);
		Assert.isTrue(elementList.getLength() >= 1);
		Assert.isTrue(elementList.item(0) instanceof Element);
		return (Element) elementList.item(0);
	}

	public void afterPropertiesSet() throws Exception {
		TEIDocument template = createTemplateDocument();
		Assert.notNull(headerTemplate = (Element) template.getHeader().cloneNode(true));
		Assert.notNull(textTemplate = (Element) template.getText().cloneNode(true));
		
		Document document = template.getDocument();
		namespaceCache = new XMLNamespaceCache(document, false);
		for (Node node = document.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node instanceof ProcessingInstruction) {
				ProcessingInstruction pi = (ProcessingInstruction) node;
				if ("oxygen".equals(pi.getTarget())) {
					schemaResourceURL = StringUtils.substringBetween(pi.getData(), "RNGSchema=\"", "\"");
				}
			}
		}
		Assert.hasText(schemaResourceURL);
		//Assert.notNull(SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI).newSchema(new URL(schemaResourceURL)));
		
		serializationTemplates = TransformerFactory.newInstance().newTemplates(
				new StreamSource(TEI_SERIALIZATION_RESOURCE.getInputStream()));
	}

	public TEIDocument createTemplateDocument() {
		try {
			return new TEIDocument(XMLUtil.build(TEI_TEMPLATE_RESOURCE.getInputStream()));
		} catch (SAXException e) {
			throw ErrorUtil.fatal("XML error while creating TEI template", e);
		} catch (IOException e) {
			throw ErrorUtil.fatal("I/O error while creating TEI template", e);
		}
	}
}
