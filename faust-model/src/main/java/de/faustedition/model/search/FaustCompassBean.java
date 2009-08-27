package de.faustedition.model.search;

import java.io.File;

import org.compass.core.Property;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.rsem.builder.RSEM;
import org.compass.core.mapping.rsem.builder.ResourceMappingBuilder;
import org.compass.spring.LocalCompassBean;
import org.compass.spring.LocalCompassBeanPostProcessor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

import de.faustedition.model.metadata.MetadataFieldDefinition;

public class FaustCompassBean extends LocalCompassBean {
	private File dataDirectory;

	@Required
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File searchIndexDirectory = new File(dataDirectory, "search-index");
		if (!searchIndexDirectory.isDirectory()) {
			Assert.isTrue(searchIndexDirectory.mkdirs(), "Cannot create search index directory");
		}

		setConnection(new FileSystemResource(searchIndexDirectory));
		setPostProcessor(new CompassConfigurationPostProcessor());

		super.afterPropertiesSet();
	}

	private class CompassConfigurationPostProcessor implements LocalCompassBeanPostProcessor {

		@Override
		public void process(CompassConfiguration config) throws ConfigurationException {
			ResourceMappingBuilder metadataResourceMapping = RSEM.resource("metadata");
			metadataResourceMapping.add(RSEM.id("path"));
			metadataResourceMapping.add(RSEM.property("repositoryType"));
			for (String metadataFieldName : MetadataFieldDefinition.REGISTRY_LOOKUP_TABLE.keySet()) {
				metadataResourceMapping.add(RSEM.property(metadataFieldName).store(Property.Store.NO));
			}
			config.addMapping(metadataResourceMapping);
		}

	}
}
