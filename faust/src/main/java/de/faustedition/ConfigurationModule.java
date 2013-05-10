package de.faustedition;

import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        try {
            Names.bindProperties(binder(), Configuration.read());
        } catch (IOException e) {
            throw new ProvisionException(e.getMessage(), e);
        }
    }

}
