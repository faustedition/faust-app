package de.faustedition;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.faustedition.http.CompactTextModule;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MarshallingModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new CompactTextModule());
        return objectMapper;
    }
}
