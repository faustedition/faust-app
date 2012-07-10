package de.faustedition.dataimport;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import de.faustedition.xml.XPathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.faustedition.xml.Namespaces.TEI_NS_URI;

public class GeneticRelationScaffolder extends Runtime implements Runnable {

	@Autowired
	private XMLStorage xml;

	public static void main(String[] args) throws Exception {
		main(GeneticRelationScaffolder.class, args);
	}

	@Override
	public void run() {
		try {
			final XPathExpression lineNumberXP = XPathUtil.xpath("//f:field[f:key[@n='41v']]/f:value/text()");
			final XPathExpression existingGeneticRefsXP = XPathUtil.xpath("count(//tei:l[@n])");

			final Pattern lineIntervalPattern = Pattern.compile("([0-9]+)-?([0-9]+)?");
			for (FaustURI source : xml.iterate(new FaustURI(FaustAuthority.XML, "/document"))) {
				final Document dd = XMLUtil.parse(xml.getInputSource(source));
				final String base = dd.getDocumentElement().getAttributeNS(XMLConstants.XML_NS_URI, "base");
				final String transcript = dd.getDocumentElement().getAttribute("transcript");
				if (Strings.isNullOrEmpty(base) || Strings.isNullOrEmpty(transcript)) {
					continue;
				}
				final FaustURI textSource = FaustURI.parse(base).resolve(transcript);

				final Document text = XMLUtil.parse(xml.getInputSource(textSource));
				final Number refCount = (Number) existingGeneticRefsXP.evaluate(text, XPathConstants.NUMBER);
				if (refCount.intValue() > 0) {
					continue;
				}

				final SortedSet<LineInterval> intervals = new TreeSet<LineInterval>();
				String lineNumbers = (String) lineNumberXP.evaluate(dd, XPathConstants.STRING);
				if (!Strings.isNullOrEmpty(lineNumbers)) {
					Matcher lineIntervalMatcher = lineIntervalPattern.matcher(lineNumbers);
					while (lineIntervalMatcher.find()) {
						final int start = Integer.valueOf(lineIntervalMatcher.group(1));
						final String end = lineIntervalMatcher.group(2);
						intervals.add(new LineInterval(start, (end == null ? start : Integer.valueOf(end))));
					}
				}
				if (intervals.isEmpty()) {
					continue;
				}

				Element textElement = XMLUtil.getChild(text.getDocumentElement(), "text");
				if (textElement == null) {
					textElement = text.createElementNS(TEI_NS_URI, "text");
					text.getDocumentElement().appendChild(textElement);
				}
				Element bodyElement = XMLUtil.getChild(textElement, "body");
				if (bodyElement == null) {
					bodyElement = text.createElementNS(TEI_NS_URI, "body");
					textElement.appendChild(bodyElement);
				}
				if (!XMLUtil.hasText(bodyElement)) {
					XMLUtil.removeChildren(bodyElement);
				}

				final Element templateDiv = text.createElementNS(TEI_NS_URI, "div");
				templateDiv.setAttribute("type", "template");
				bodyElement.appendChild(templateDiv);

				for (LineInterval li : intervals) {
					Element parent = templateDiv;
					if (!li.isSingleton()) {
						templateDiv.appendChild(parent = text.createElementNS(TEI_NS_URI, "lg"));
					}
					for (int line = li.start; line <= li.end; line++) {
						Element lineElement = text.createElementNS(TEI_NS_URI, "l");
						lineElement.setAttribute("n", Integer.toString(line));
						parent.appendChild(lineElement);
					}
				}
				System.out.printf("%s ==> %s\n", Joiner.on(", ").join(intervals), textSource);
				xml.put(textSource, text);
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private class LineInterval implements Comparable<LineInterval> {
		private final int start;
		private final int end;

		private LineInterval(int num1, int num2) {
			start = Math.min(num1, num2);
			end = Math.max(num1, num2);
		}

		private boolean isSingleton() {
			return start == end;
		}

		@Override
		public String toString() {
			return "{ " + (start == end ? "" + start : start + "; " + end) + " }";
		}

		@Override
		public int compareTo(LineInterval o) {
			return (start == o.start ? end - o.end : start - o.start);
		}

	}
}
