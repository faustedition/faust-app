package de.faustedition.graph;

import eu.interedition.text.json.JacksonDataNodeMapper;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class TextRepositoryFactoryBean extends AbstractFactoryBean<Neo4jTextRepository<JsonNode>> {

  @Autowired
  private FaustGraph faustGraph;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Class<?> getObjectType() {
    return Neo4jTextRepository.class;
  }

  @Override
  protected Neo4jTextRepository<JsonNode> createInstance() throws Exception {
    return new Neo4jTextRepository<JsonNode>(JsonNode.class, new JacksonDataNodeMapper<JsonNode>(objectMapper), faustGraph.getDb(), false);
  }
}
