package de.faustedition.model.facsimile;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

public class FacsimileStore implements InitializingBean {
	private String dataDirectory;

	private File lowResolutionImageDirectory;
	private File highResolutionImageDirectory;

	@Required
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File imageDirectory = new File(dataDirectory, "images");
		Assert.isTrue(imageDirectory.isDirectory() && lowResolutionImageDirectory.canRead(), "Cannot access image directory");

		lowResolutionImageDirectory = new File(imageDirectory, "jpg");
		Assert.isTrue(lowResolutionImageDirectory.isDirectory() && lowResolutionImageDirectory.canRead(), "Cannot access low-res image directory");

		highResolutionImageDirectory = new File(imageDirectory, "tif");
		Assert.isTrue(highResolutionImageDirectory.isDirectory() && highResolutionImageDirectory.canRead(), "Cannot access high-res image directory");
	}

}
