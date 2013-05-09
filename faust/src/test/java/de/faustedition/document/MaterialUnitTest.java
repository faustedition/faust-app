package de.faustedition.document;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.graph.Graph;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaterialUnitTest extends AbstractContextTest {

	@Autowired
	private GraphDatabaseService graphDatabaseService;


	@Test
	public void list() {
        Graph.execute(graphDatabaseService, new Graph.Transaction<Object>() {
            @Override
            public Object execute(Graph graph) throws Exception {
                for (Document document : Document.find(graph.db(), "XVIII,1")) {
                    System.out.println(Arrays.toString(document.getMetadata("wa-id")) + ", " + Arrays.toString(document.getMetadata("callnumber")));
                }
                return null;
            }
        });
	}
}
