/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.tei;

import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;

@Component
public class TeiTemplater extends Runtime implements Runnable {
	private static final FaustURI TEMPLATE_SOURCE = new FaustURI(FaustAuthority.XML, "/template/tei.xml");

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Override
	public void run() {
		try {
			final XPathExpression piXP = XPathUtil.xpath("/processing-instruction()");
			final XPathExpression handNotesXP = XPathUtil.xpath("//tei:teiHeader/tei:profileDesc/tei:handNotes");
			final XPathExpression charDeclXP = XPathUtil.xpath("//tei:teiHeader/tei:encodingDesc/tei:charDecl");

			final Document template = XMLUtil.parse(xml.getInputSource(TEMPLATE_SOURCE));

			final ProcessingInstruction testPi = new NodeListWrapper<ProcessingInstruction>(piXP, template)
					.singleResult(ProcessingInstruction.class);
			final Element testHandNotes = new NodeListWrapper<Element>(handNotesXP, template)
					.singleResult(Element.class);
			final Element testCharDecl = new NodeListWrapper<Element>(charDeclXP, template).singleResult(Element.class);
			if (testPi == null || testHandNotes == null || testCharDecl == null) {
				logger.error(String.format("Template is incomplete: [%s, %s, %s]", testPi, testHandNotes,
						testCharDecl));
				return;
			}

			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
				if (!source.getPath().endsWith(".xml")) {
					continue;
				}

				logger.debug("Updating templated sections in " + source);

				try {
					final Document current = XMLUtil.parse(xml.getInputSource(source));
					Element docElement = current.getDocumentElement();
					if (!"TEI".equals(docElement.getLocalName())
							|| !Namespaces.TEI_NS_URI.equals(docElement.getNamespaceURI())) {
						continue;
					}

					for (ProcessingInstruction pi : new NodeListWrapper<ProcessingInstruction>(piXP, current)) {
						pi.getParentNode().removeChild(pi);
					}
					for (ProcessingInstruction pi : new NodeListWrapper<ProcessingInstruction>(piXP, template)) {
						current.insertBefore(current.adoptNode(pi.cloneNode(true)), docElement);
					}
					replaceElements(handNotesXP, current, template);
					replaceElements(charDeclXP, current, template);

					xml.put(source, current);
				} catch (SAXException e) {
					logger.debug("XML error while templating " + source, e);
				} catch (IOException e) {
					logger.debug("I/O error while templating " + source, e);
				} catch (TransformerException e) {
					logger.error("XML serialization error while templating " + source, e);
				}
			}
		} catch (IOException e) {
			logger.error("I/O error while initializing TEI template " + TEMPLATE_SOURCE, e);
		} catch (SAXException e) {
			logger.error("XML error while initializing TEI template " + TEMPLATE_SOURCE, e);
		} catch (XPathExpressionException e) {
			logger.error("XPath error while templating TEI documents", e);
		}

	}

	private void replaceElements(XPathExpression xp, Document in, Document from) throws XPathExpressionException {
		final NodeListWrapper<Element> substitutes = new NodeListWrapper<Element>(xp, from);
		for (Element element : new NodeListWrapper<Element>(xp, in)) {
			final Node parent = element.getParentNode();
			for (Element substitute : substitutes) {
				parent.insertBefore(in.adoptNode(substitute.cloneNode(true)), element);
			}
			parent.removeChild(element);
		}
	}

	public static void main(String[] args) throws Exception {
		main(TeiTemplater.class, args);
		System.exit(0);
	}

}
