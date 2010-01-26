package de.faustedition.model.facsimile;

import static de.faustedition.model.DatastoreConfiguration.subDirectory;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.faustedition.model.DatastoreConfiguration;

@Configuration
public class FacsimileConfiguration {
	@Autowired
	private DatastoreConfiguration datastoreConfiguration;

	@Value("#{config['im.convert']}")
	private String conversionTool;

	@Bean
	public FacsimileManager facsimileImageDao() throws IOException {
		if (!new File(conversionTool).isFile()) {
			throw new IllegalArgumentException("ImageMagick convert path '" + conversionTool + "' does not exist.");
		}

		File imageDirectory = datastoreConfiguration.dataSubDirectory("images");

		FacsimileManager facsimileManager = new FacsimileManager();
		facsimileManager.setConversionTool(conversionTool);
		facsimileManager.setHighResolutionImageDirectory(subDirectory(imageDirectory, "tif"));
		facsimileManager.setLowResolutionImageDirectory(subDirectory(imageDirectory, "jpg"));
		facsimileManager.setThumbnailDirectory(subDirectory(imageDirectory, "thumb"));

		return facsimileManager;
	}
}
