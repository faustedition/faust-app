package de.faustedition.model;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

public class EnvironmentBasedModelConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_OVERRIDE);
		String environment = System.getProperty("faust.environment", "development");
		setLocation(new ClassPathResource(String.format("/faust-model-settings-%s.properties", environment)));
	}

}
