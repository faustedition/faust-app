package de.faustedition;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import de.faustedition.json.CompactTextModule;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class ObjectMapperFactoryBean extends AbstractFactoryBean<ObjectMapper> {

  @Override
  public Class<?> getObjectType() {
    return ObjectMapper.class;
  }

  @Override
  protected ObjectMapper createInstance() throws Exception {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new CompactTextModule());
    return objectMapper;
  }
}
