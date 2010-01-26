package de.faustedition.model;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatastoreConfiguration {
	@Value("#{config['data.home']}")
	private String dataDirectoryPath;

	@Bean
	@Qualifier("data")
	public File dataDirectory() throws IOException {
		File dataDirectoryFile = new File(dataDirectoryPath);
		if (!dataDirectoryFile.isDirectory()) {
			throw new IllegalArgumentException("Data path '" + dataDirectoryPath + "' does not point to a directory ");
		}
		return dataDirectoryFile;
	}

	public File dataSubDirectory(String name) throws IOException {
		return subDirectory(dataDirectory(), name);
	}

	public static File subDirectory(File directory, String name) throws IOException {
		File subDirectory = new File(directory, name);
		if (subDirectory.isDirectory() || subDirectory.mkdirs()) {
			return subDirectory;
		}
		throw new IllegalStateException("Cannot create subdirectory '" + subDirectory.getAbsolutePath() + "'");

	}
}
