package de.abohnenkamp.paralipomena;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.faustedition.util.LoggingUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/faust-model-context.xml", "/faust-paralipomena-import-context.xml" })
public class DissertationTextTest {
	@Autowired
	private DissertationText dissertationText;

	@Test
	public void extract() throws Exception {
		dissertationText.extractParalipomena();
	}

	@Test
	public void scanElements() {
		LoggingUtil.LOG.info(StringUtils.join(scanElements(dissertationText.getDocument()), ", "));
	}

	private Set<String> scanElements(Node node) {
		Set<String> elementNames = new TreeSet<String>();
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node current = childNodes.item(i);
			if (current.getNodeType() == Node.ELEMENT_NODE) {
				elementNames.add(current.getLocalName());
			}
			elementNames.addAll(scanElements(current));
		}
		return elementNames;
	}
}
