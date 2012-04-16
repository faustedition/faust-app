package de.faustedition.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.faustedition.document.XMLDocumentImageLinker.IdGenerator;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;

public class DocumentImageLinkerTest {

	public static void printXml (Document doc, OutputStream stream) {

		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(new DOMSource(doc), 
			         new StreamResult(new OutputStreamWriter(stream, "UTF-8")));
		} catch (Exception e) {
			throw new RuntimeException("Oops!", e);
		}
	}
	
	@Test
	public void testLink() throws SAXException, IOException,
			XPathExpressionException, TransformerException {

		IdGenerator newIds = new IdGenerator() {
			private int index = 0;

			@Override
			public String next() {
				return String.format("l%d", index++);
			}
		};
		
		IdGenerator tmpIds = new IdGenerator() {
			private int index = 0;

			@Override
			public String next() {
				return String.format("lineNumber%d", index++);
			}
		};
		

		String xmlSrc = 
			"<root> \n" + 
			" <line>                     lineNumber0,     referenced, [id0]    </line>\n" + 
			" <line xml:id=\"l2\">       lineNumber1,     referenced, l2       </line>\n" + 
			" <line>                     lineNumber2,     referenced, [id2]    </line>\n" + 
			" <line xml:id=\"someId\">   lineNumber3, not referenced, someId   </line>\n" + 
			" <line>                     lineNumber4,     referenced, [id4]    </line>\n" +
			" <line>                     lineNumber5, not referenced           </line>\n" +
			"</root>";

		InputStream in = new ByteArrayInputStream(xmlSrc.getBytes(Charset
				.forName("UTF-8")));
		Document xmlDoc = XMLUtil.parse(in);
		in.close();

		String xlinkNS = "http://www.w3.org/1999/xlink" ;	
		
		String svgSrc = 
			"<svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
			"     xmlns:xlink=\"" + xlinkNS + "\"> \n" + 
			" <g/>\n" + 
			" <g xlink:href=\"lineNumber2\"/>\n" + 
			" <g xlink:href=\"lineNumber0\"/>\n" + 
			" <g xlink:href=\"lineNumber4\">content</g>\n" + 
			" <g xlink:href=\"lineNumber1\"/>\n" + 
			"</svg>\n";

		in = new ByteArrayInputStream(svgSrc.getBytes(Charset.forName("UTF-8")));
		Document svgDoc = XMLUtil.parse(in);
		in.close();

		javax.xml.xpath.XPathExpression lines = XPathUtil.xpath("//line");

		XMLDocumentImageLinker.link(xmlDoc, tmpIds, lines, svgDoc, newIds);

		printXml(xmlDoc, System.out);
		
		Node xmlRoot = xmlDoc.getFirstChild();

		int index = 0;
		String id0 = null;
		String id2 = null;
		String id4 = null;
		for (int i = 0; i < xmlRoot.getChildNodes().getLength(); i++) {
			Node line = xmlRoot.getChildNodes().item(i);
			if (line.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				Attr id = (Attr) (line.getAttributes().getNamedItemNS(
						XMLDocumentImageLinker.XMLNS, "id"));

				switch (index) {
				case 0:
					assertNotNull(id);
					id0 = id.getValue();
					break;
				case 1:
					assertEquals("l2", id.getValue());
					break;
				case 2:
					assertNotNull(id);
					id2 = id.getValue();
					break;
				case 3:
					assertEquals("someId", id.getValue());
					break;
				case 4:
					assertNotNull(id);
					id4 = id.getValue();
					break;
				case 5:
					assertNull(id);
					break;

				}
				index++;
			}
		}
		
		System.out.println();
		System.out.println();
		printXml(svgDoc, System.out);
		
		Node svgRoot = svgDoc.getFirstChild();

		index = 0;
		for (int i = 0; i < svgRoot.getChildNodes().getLength(); i++) {
			Node el = svgRoot.getChildNodes().item(i);
			if (el.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
				Attr link = (Attr) (el.getAttributes().getNamedItemNS(
						xlinkNS, "href"));
				//assertTrue("Element doesn't have an ID!", link != null);

				switch (index) {
				case 0:
					assertTrue(link == null);
					break;
				case 1:
					assertEquals("#" + id2, link.getValue());
					break;
				case 2:
					assertEquals("#" + id0, link.getValue());
					break;
				case 3:
					assertEquals("#" + id4, link.getValue());
					break;
				case 4:
					assertEquals("#l2", link.getValue());
					break;
				}
				index++;
			}
		}


	}

}
