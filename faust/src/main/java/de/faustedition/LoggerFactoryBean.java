package de.faustedition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class LoggerFactoryBean extends AbstractFactoryBean<Logger> {
	@Override
	public Class<?> getObjectType() {
		return Logger.class;
	}

	@Override
	protected Logger createInstance() throws Exception {
		return LoggerFactory.getLogger(getClass().getPackage().getName());
	}
}
