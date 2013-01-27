package de.faustedition;

import eu.interedition.text.json.TextModule;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ObjectMapperFactoryBean extends AbstractFactoryBean<ObjectMapper> {

  @Override
  public Class<?> getObjectType() {
    return ObjectMapper.class;
  }

  @Override
  protected ObjectMapper createInstance() throws Exception {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new TextModule());
    return objectMapper;
  }
}
