package de.faustedition.tei;

import static de.faustedition.tei.EncodedTextDocument.xpath;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.xml.xpath.XPathExpression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.faustedition.Log;
import de.faustedition.report.Report;
import de.faustedition.report.ReportManager;
import de.faustedition.xml.NodeListIterable;
import de.faustedition.xml.XmlStore;
import de.faustedition.xml.XmlUtil;

@Service
public class WhitespaceNormalizationReporter implements Runnable {

	private static final XPathExpression TO_CHECK = xpath("//*[(ancestor::tei:text or ancestor::ge:document) and not(@xml:space)]");

	private static final Set<String> IGNORED_TAGS = Sets.newTreeSet(Lists.newArrayList("app", "back", "body", "choice",//
			"div", "docTitle", "fix", "front", "fw", "g", "group", "lg", "overw", "patch", "sp", "subst",//
			"surface", "text", "titlePage", "titlePart", "used", "zone"));

	private static final Set<String> IGNORED_EMPTY_ELEMS = Sets.newTreeSet(Lists.newArrayList("addSpan", "anchor", "cb",//
			"certainty", "damageSpan", "delSpan", "gap", "grBrace", "grLine", "handShift", "ins", "join", "lb",//
			"pb", "space", "st", "undo", "p"));

	@Autowired
	private XmlStore store;

	@Autowired
	private ReportManager reportManager;
	
	@Override
	public void run() {
		try {
			StringBuilder reportContents = new StringBuilder();			
			for (URI uri : store) {
				String path = uri.getPath();
				if (!path.endsWith(".xml") || path.endsWith("001.xml") || path.endsWith("0001.xml")) {
					continue;
				}
				Document document = store.get(uri);

				for (Element e : new NodeListIterable<Element>(TO_CHECK, document)) {
					String localName = e.getLocalName();
					if (IGNORED_TAGS.contains(localName)) {
						continue;
					}

					NodeList children = e.getChildNodes();

					if (children.getLength() == 0 && IGNORED_EMPTY_ELEMS.contains(localName)) {
						continue;
					}

					String origContent = "";
					if (children.getLength() > 0) {
						StringBuilder content = new StringBuilder();
						for (Node child : new NodeListIterable<Node>(children)) {
							if (Node.TEXT_NODE == child.getNodeType()) {
								content.append(child.getTextContent());
							}
						}
						origContent = content.toString();
					}

					if (children.getLength() == 0 || (origContent.length() > 0 && origContent.trim().length() == 0)) {
						reportContents.append(uri.toString() + ": " + //
								XmlUtil.toString(XmlUtil.stripNamespace(e)) + "\n");
					}
				}
			}
			
			Report report = new Report("whitespace_normalization");
			report.setBody(reportContents.length() == 0 ? null : reportContents.toString());
			reportManager.send(report);
		} catch (IOException e) {
			Log.fatalError(e, "I/O error while reporting on whitespace normalization");
		}
	}

}
