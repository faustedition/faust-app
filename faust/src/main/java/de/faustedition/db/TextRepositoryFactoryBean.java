package de.faustedition.db;

import eu.interedition.text.h2.H2TextRepository;
import eu.interedition.text.json.JacksonDataStreamMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class TextRepositoryFactoryBean extends AbstractFactoryBean<H2TextRepository<JsonNode>> {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public Class<?> getObjectType() {
    return H2TextRepository.class;
  }

  @Override
  protected H2TextRepository<JsonNode> createInstance() throws Exception {
    return new H2TextRepository<JsonNode>(JsonNode.class, new JacksonDataStreamMapper<JsonNode>(objectMapper), dataSource, true);
  }
}
