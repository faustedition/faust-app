package de.faustedition.facsimile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

@Configuration
public class FacsimileConfiguration {
	@Value("#{facsimileConfig['facsimile.home']}")
	private String facsimileHomeDirPath;

	@Autowired
	@Qualifier("facsimileConfig")
	private Properties configProperties;

	@Bean
	public FacsimileManager facsimileManager() throws IOException {
		File imageDirectory = new File(facsimileHomeDirPath);
		Assert.isTrue(imageDirectory.isDirectory(), "Facsimile path '" + facsimileHomeDirPath
				+ "' does not point to a directory ");

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

	private static File subDirectory(File directory, String name) throws IOException {
		File subDirectory = new File(directory, name);
		if (subDirectory.isDirectory() || subDirectory.mkdirs()) {
			return subDirectory;
		}
		throw new IllegalStateException("Cannot create subdirectory '" + subDirectory.getAbsolutePath() + "'");

	}

}
