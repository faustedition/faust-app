package de.faustedition.tei;

import com.google.common.base.Strings;
import de.faustedition.EmailReporter;
import de.faustedition.EmailReporter.ReportCreator;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;
import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpression;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_URI;

@Component
public class TeiEncodingReporter extends Runtime implements Runnable {
	private static final String[] STATI = new String[] { "kodiert", "encoded", "proof-read", "published", "n/a" };

	@Autowired
	private XMLStorage xml;

	@Autowired
	private EmailReporter reporter;

	@Autowired
	private Logger logger;

	@Override
	public void run() {
		try {
			final Map<String, Integer> documentStatusMap = new HashMap<String, Integer>();
			final Map<String, Integer> textStatusMap = new HashMap<String, Integer>();
			for (String status : STATI) {
				documentStatusMap.put(status, 0);
				textStatusMap.put(status, 0);
			}
			
			final XPathExpression changeXP = XPathUtil.xpath("//tei:teiHeader//tei:revisionDesc/tei:change");
			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				try {
					final Document document = XMLUtil.parse(xml.getInputSource(source));
					final Element docElement = document.getDocumentElement();
					if (!"TEI".equals(docElement.getLocalName())
							|| !TEI_NS_URI.equals(docElement.getNamespaceURI())) {
						continue;
					}

					String status = null;
					for (Element change : new NodeListWrapper<Element>(changeXP, document)) {
						final String changeStr = change.getTextContent().toLowerCase();
						for (String statusCandidate : STATI) {
							if (changeStr.contains(statusCandidate)) {
								status = statusCandidate;
							}
						}
					}
					
					if (status == null) {
						status = "n/a";
					}
					
					Map<String, Integer> target = (source.isTextEncodingDocument() ? textStatusMap : documentStatusMap);
					target.put(status, target.get(status) + 1);
				} catch (SAXException e) {
					logger.debug("XML error while checking encoding status of " + source, e);
				} catch (IOException e) {
					logger.debug("I/O error while checking encoding status of " + source, e);
				}
			}

			reporter.send("TEI encoding status", new ReportCreator() {

                		@SuppressWarnings("unchecked")
				@Override
				public void create(PrintWriter body) {

					for (Map<String, Integer> map : new Map[] { documentStatusMap, textStatusMap }) {
						if (documentStatusMap == map) {
							body.println("Documentary transcripts:");
							body.println();
						} else {
							body.println();							
							body.println("Textual transcripts:");
							body.println();
						}
						
						body.println(Strings.repeat("=", 40));
						boolean firstLine = true;
						for (String status : STATI) {
							if (!firstLine) {
								body.println(Strings.repeat("-", 40));							
							}
							firstLine = false;
							String count = Integer.toString(map.get(status));
							body.println(status + Strings.padStart(count, (40 - status.length()), ' '));
						}
						body.println(Strings.repeat("=", 40));
					}
				}
			});
		} catch (EmailException e) {
			logger.error("E-mail error while reporting on encoding status", e);
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
