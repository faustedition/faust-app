package de.faustedition.document;

import de.faustedition.AbstractContextTest;
import de.faustedition.graph.FaustGraph;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaterialUnitTest extends AbstractContextTest {

	@Autowired
	private FaustGraph graph;


	@Test
	public void list() {
		for (Archive archive : graph.getArchives()) {
			System.out.printf("%s: %d archival unit(s)\n", archive.getId(), archive.size());
		}

		for (MaterialUnit mu : graph.getMaterialUnits()) {
			for (String property : mu.node.getPropertyKeys()) {
				final Object propertyValue = mu.node.getProperty(property);
				final String value = (propertyValue instanceof Object[] ? Arrays.toString((Object[]) propertyValue)  : propertyValue.toString());
				//System.out.printf("%s :: %s\n", Strings.padEnd(property, 40, ' '), value);
			}
			//System.out.println(Strings.repeat("=", 80));
		}
		System.out.printf("%d material unit(s)", graph.getMaterialUnits().size()).println();
	}
}
