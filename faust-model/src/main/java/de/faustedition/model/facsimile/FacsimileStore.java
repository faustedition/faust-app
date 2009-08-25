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
	private String dataDirectory;

	private File lowResolutionImageDirectory;
	private File highResolutionImageDirectory;
	private File thumbnailDirectory;
	private int thumbnailHeight = 150;
	private int thumbnailWidth = 75;
	private String imageMagickConvertCommand;

	@Required
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Required
	public void setImageMagickConvertCommand(String imageMagickConvertCommand) {
		this.imageMagickConvertCommand = imageMagickConvertCommand;
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
			File thumbnailSourceFile = new File(getBaseDirectory(FacsimileResolution.LOW), path + FacsimileResolution.LOW.getSuffix());

			if (!thumbnailSourceFile.isFile() || !thumbnailSourceFile.canRead()) {
				return null;
			}

			if (!imageFile.getParentFile().isDirectory()) {
				imageFile.getParentFile().mkdirs();
				if (!imageFile.getParentFile().isDirectory()) {
					throw ErrorUtil.fatal("Cannot create directory for thumbnail '" + imageFile.getAbsolutePath() + "'");
				}
			}

			try {
				final Process convertProcess = new ProcessBuilder(imageMagickConvertCommand, thumbnailSourceFile.getAbsolutePath(), "-resize", thumbnailWidth + "x" + thumbnailHeight,
						"-").start();
				Thread conversionResultReaderThread = new Thread(new Runnable() {

					@Override
					public void run() {
						InputStream convertOutput = null;
						OutputStream thumbnailStream = null;
						try {
							convertOutput = convertProcess.getInputStream();
							thumbnailStream = new FileOutputStream(imageFile);
							IOUtils.copy(convertOutput, thumbnailStream);
						} catch (IOException e) {
							IOUtils.closeQuietly(thumbnailStream);
							imageFile.delete();
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
				} catch (InterruptedException e) {
				}

				if (conversionResult == 0) {
					return imageFile;
				}
			} catch (IOException e1) {
				ErrorUtil.fatal("I/O error while generating thumbnail '" + imageFile.getAbsolutePath() + "'", e1);
			}
			imageFile.delete();
		}
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
