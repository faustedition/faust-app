package de.faustedition.model.facsimile;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import de.faustedition.model.Facsimile;
import de.faustedition.model.Transcription;

public class FacsimileStore implements InitializingBean {
	public enum Resolution {
		LOW, HIGH
	};

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
		Assert.isTrue(imageDirectory.isDirectory() && imageDirectory.canRead(), "Cannot access image directory");

		lowResolutionImageDirectory = new File(imageDirectory, "jpg");
		Assert.isTrue(lowResolutionImageDirectory.isDirectory() && lowResolutionImageDirectory.canRead(), "Cannot access low-res image directory");

		highResolutionImageDirectory = new File(imageDirectory, "tif");
		Assert.isTrue(highResolutionImageDirectory.isDirectory() && highResolutionImageDirectory.canRead(), "Cannot access high-res image directory");
	}

	public Facsimile find(Transcription transcription) {
		return find(transcription, Resolution.LOW);
	}

	public Facsimile find(Transcription transcription, Resolution resolution) {
		File imageFile = new File(getBaseDirectory(resolution), transcription.getPath() + getImageSuffix(resolution));
		if (imageFile.isFile() && imageFile.canRead()) {
			return new Facsimile(transcription, imageFile);
		}

		return null;
	}

	protected File getBaseDirectory(Resolution resolution) {
		return (resolution.equals(Resolution.LOW) ? lowResolutionImageDirectory : highResolutionImageDirectory);
	}

	protected String getImageSuffix(Resolution resolution) {
		return (resolution.equals(Resolution.LOW) ? ".jpg" : ".tif");
	}

}
