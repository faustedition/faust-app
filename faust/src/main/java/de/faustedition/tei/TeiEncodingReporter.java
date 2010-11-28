package de.faustedition.tei;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.mail.EmailException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.EmailReporter;
import de.faustedition.EmailReporter.ReportCreator;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;

@Singleton
public class TeiEncodingReporter extends Runtime implements Runnable {
	private static final String[] STATI = new String[] { "encoded", "proof-read", "published", "n/a" };
	private final XMLStorage xml;
	private final EmailReporter reporter;
	private final Logger logger;

	@Inject
	public TeiEncodingReporter(XMLStorage xml, EmailReporter reporter, Logger logger) {
		this.xml = xml;
		this.reporter = reporter;
		this.logger = logger;
	}

	@Override
	public void run() {
		try {
			final XPathExpression changeXP = XPathUtil.xpath("//tei:teiHeader//tei:revisionDesc/tei:change");
			final Map<String, Integer> statusMap = new HashMap<String, Integer>();
			for (String status : STATI) {
				statusMap.put(status, 0);
			}
			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				try {
					final Document document = XMLUtil.parse(xml.getInputSource(source));
					final Element docElement = document.getDocumentElement();
					if (!"TEI".equals(docElement.getLocalName())
							|| !TEI_NS_URI.equals(docElement.getNamespaceURI())) {
						continue;
					}

					boolean statusFound = false;
					for (Element change : new NodeListWrapper<Element>(changeXP, document)) {
						final String changeStr = change.getTextContent().toLowerCase();
						for (String status : STATI) {
							if (changeStr.contains(status)) {
								statusMap.put(status, statusMap.get(status) + 1);
								statusFound = true;
								break;
							}
						}
						if (statusFound) {
							break;
						}
					}
					if (!statusFound) {
						statusMap.put("n/a", statusMap.get("n/a") + 1);
					}
				} catch (SAXException e) {
					logger.log(Level.FINE, "XML error while checking encoding status of " + source, e);
				} catch (IOException e) {
					logger.log(Level.FINE, "I/O error while checking encoding status of " + source, e);
				}
			}

			reporter.send("TEI encoding status", new ReportCreator() {

				@Override
				public void create(PrintWriter body) {
					body.println(Strings.repeat("=", 40));
					boolean firstLine = true;
					for (String status : STATI) {
						if (!firstLine) {
							body.println(Strings.repeat("-", 40));							
						}
						firstLine = false;
						String count = Integer.toString(statusMap.get(status));
						body.println(status + Strings.padStart(count, (40 - status.length()), ' '));
					}
					body.println(Strings.repeat("=", 40));
				}
			});
		} catch (XPathExpressionException e) {
			logger.log(Level.SEVERE, "XPath error while reporting on encoding status", e);
		} catch (EmailException e) {
			logger.log(Level.SEVERE, "E-mail error while reporting on encoding status", e);
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			main(TeiEncodingReporter.class, args);
		} finally {
			System.exit(0);
		}
	}

}
