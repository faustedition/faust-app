package de.faustedition.model.facsimile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import de.faustedition.model.store.ContentStore;
import de.faustedition.util.ErrorUtil;

public class FacsimileStore implements InitializingBean {
	private File dataDirectory;

	private File lowResolutionImageDirectory;
	private File highResolutionImageDirectory;
	private File thumbnailDirectory;
	private int thumbnailHeight = 150;
	private int thumbnailWidth = 75;
	private String imageMagickConvertCommand;
	private String[] conversionTools;

	@Required
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Required
	public void setConversionTools(String[] conversionTools) {
		this.conversionTools = conversionTools;
	}

	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	public File find(String path) {
		return find(path, FacsimileResolution.LOW);
	}

	public File find(String path, FacsimileResolution resolution) {
		path = ContentStore.normalizePath(path);
		final File imageFile = new File(getBaseDirectory(resolution), path + resolution.getSuffix());

		if (imageFile.isFile() && imageFile.canRead()) {
			return imageFile;
		}

		if (resolution == FacsimileResolution.THUMB) {
			File sourceFile = new File(getBaseDirectory(FacsimileResolution.LOW), path + FacsimileResolution.LOW.getSuffix());

			if (!sourceFile.isFile() || !sourceFile.canRead()) {
				return null;
			}

			return createThumbnail(sourceFile, imageFile);
		}

		return null;
	}

	private File createThumbnail(File source, final File thumbnailFile) {
		if (!thumbnailFile.getParentFile().isDirectory()) {
			thumbnailFile.getParentFile().mkdirs();
			if (!thumbnailFile.getParentFile().isDirectory()) {
				throw ErrorUtil.fatal("Cannot create directory for thumbnail '" + thumbnailFile.getAbsolutePath() + "'");
			}
		}

		try {
			final Process convertProcess = new ProcessBuilder(imageMagickConvertCommand, source.getAbsolutePath(), "-resize", thumbnailWidth + "x" + thumbnailHeight, "-").start();
			Thread conversionResultReaderThread = new Thread(new Runnable() {

				@Override
				public void run() {
					InputStream convertOutput = null;
					OutputStream thumbnailStream = null;
					try {
						convertOutput = convertProcess.getInputStream();
						thumbnailStream = new FileOutputStream(thumbnailFile);
						IOUtils.copy(convertOutput, thumbnailStream);
					} catch (IOException e) {
						IOUtils.closeQuietly(thumbnailStream);
						thumbnailFile.delete();
					} finally {
						IOUtils.closeQuietly(thumbnailStream);
						IOUtils.closeQuietly(convertOutput);
					}
				}
			});
			conversionResultReaderThread.setDaemon(true);
			conversionResultReaderThread.start();

			int conversionResult = -1;
			try {
				conversionResult = convertProcess.waitFor();
			} catch (InterruptedException e) {
			}

			try {
				conversionResultReaderThread.join();
			} catch (InterruptedException ie) {
			}

			if (conversionResult == 0) {
				return thumbnailFile;
			}
		} catch (IOException ioe) {
			ErrorUtil.fatal("I/O error while generating thumbnail '" + thumbnailFile.getAbsolutePath() + "'", ioe);
		}
		thumbnailFile.delete();
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File imageDirectory = new File(dataDirectory, "images");
		Assert.isTrue(imageDirectory.isDirectory() && imageDirectory.canRead(), "Cannot access image directory");

		lowResolutionImageDirectory = new File(imageDirectory, "jpg");
		Assert.isTrue(lowResolutionImageDirectory.isDirectory() && lowResolutionImageDirectory.canRead(), "Cannot access low-res image directory");

		highResolutionImageDirectory = new File(imageDirectory, "tif");
		Assert.isTrue(highResolutionImageDirectory.isDirectory() && highResolutionImageDirectory.canRead(), "Cannot access high-res image directory");

		File cacheDirectory = new File(dataDirectory, "cache/images");
		cacheDirectory.mkdirs();
		Assert.isTrue(cacheDirectory.isDirectory() && cacheDirectory.canWrite(), "Cannot write to cache directory");

		thumbnailDirectory = new File(cacheDirectory, "thumbnails");
		thumbnailDirectory.mkdirs();
		Assert.isTrue(thumbnailDirectory.isDirectory() && thumbnailDirectory.canWrite(), "Cannot write to thumbnail cache directory");

		for (String conversionTool : conversionTools) {
			File conversionToolFile = new File(conversionTool);
			if (conversionToolFile.isFile()) {
				imageMagickConvertCommand = conversionToolFile.getAbsolutePath();
			}
		}
		Assert.notNull(imageMagickConvertCommand, "No ImageMagick 'convert' command could by found on this system");
	}

	protected File getBaseDirectory(FacsimileResolution resolution) {
		switch (resolution) {
		case LOW:
			return lowResolutionImageDirectory;
		case HIGH:
			return highResolutionImageDirectory;
		case THUMB:
			return thumbnailDirectory;
		}

		throw new IllegalArgumentException(resolution.toString());
	}
}
