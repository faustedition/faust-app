package de.faustedition.document;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.graph.FaustGraph;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaterialUnitTest extends AbstractContextTest {

	@Autowired
	private FaustGraph graph;


	@Test
	public void list() {
		for (Document document : Document.find(graph.getDb(), "XVIII,1")) {
			System.out.println(Arrays.toString(document.getMetadata("wa-id")) + ", " + Arrays.toString(document.getMetadata("callnumber")));
		}
	}
}
