package de.faustedition.model.facsimile;

import static de.faustedition.model.DatastoreConfiguration.subDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;

import de.faustedition.model.DatastoreConfiguration;

@Configuration
public class FacsimileConfiguration {
	@Autowired
	private DatastoreConfiguration datastoreConfiguration;

	@Autowired
	@Qualifier("config")
	private Properties configProperties;
	
	@Bean
	public FacsimileManager facsimileManager() throws IOException {
		File imageDirectory = datastoreConfiguration.dataSubDirectory("images");

		Map<FacsimileResolution, File> baseDirectories = Maps.newHashMap();
		baseDirectories.put(FacsimileResolution.HIGH, subDirectory(imageDirectory, "tif"));
		baseDirectories.put(FacsimileResolution.LOW, subDirectory(imageDirectory, "jpg"));
		baseDirectories.put(FacsimileResolution.THUMB, subDirectory(imageDirectory, "thumb"));

		Map<FacsimileResolution, String> conversionCommands = Maps.newHashMap();
		for (FacsimileResolution resolution : FacsimileResolution.values()) {
			String configProperty = "facsimile.convert." + resolution.toString();
			if (configProperties.containsKey(configProperty)) {
				conversionCommands.put(resolution, configProperties.getProperty(configProperty));
			}
		}

		FacsimileManager facsimileManager = new FacsimileManager();
		facsimileManager.setConversionCommands(conversionCommands);
		facsimileManager.setBaseDirectories(baseDirectories);
		return facsimileManager;
	}
}
