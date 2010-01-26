package de.faustedition.model.facsimile;

import static de.faustedition.model.facsimile.FacsimileResolution.LOW;
import static de.faustedition.model.facsimile.FacsimileResolution.THUMB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import de.faustedition.util.ErrorUtil;

public class FacsimileManager {
	private String conversionTool;
	private File lowResolutionImageDirectory;
	private File highResolutionImageDirectory;
	private File thumbnailDirectory;
	private int thumbnailHeight = 150;
	private int thumbnailWidth = 75;

	@Required
	public void setConversionTool(String conversionTool) {
		this.conversionTool = conversionTool;
	}
	
	@Required
	public void setLowResolutionImageDirectory(File lowResolutionImageDirectory) {
		this.lowResolutionImageDirectory = lowResolutionImageDirectory;
	}
	
	@Required
	public void setHighResolutionImageDirectory(File highResolutionImageDirectory) {
		this.highResolutionImageDirectory = highResolutionImageDirectory;
	}
	
	@Required
	public void setThumbnailDirectory(File thumbnailDirectory) {
		this.thumbnailDirectory = thumbnailDirectory;
	}
	
	public void setThumbnailHeight(int thumbnailHeight) {
		this.thumbnailHeight = thumbnailHeight;
	}

	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}

	@SuppressWarnings("unchecked")
	public SortedSet<File> findImageFiles(final FacsimileResolution resolution) {
		SortedSet<File> imageFileSet = new TreeSet<File>(new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		imageFileSet.addAll(FileUtils.listFiles(getBaseDirectory(resolution), new AbstractFileFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(resolution.getSuffix());
			}

		}, TrueFileFilter.INSTANCE));
		return imageFileSet;
	}

	public File findImageFile(Facsimile facsimile) {
		return findImageFile(facsimile, LOW);
	}

	public File findImageFile(String path) {
		return findImageFile(path, LOW);
	}

	public File findImageFile(String path, FacsimileResolution resolution) {
		final File imageFile = new File(getBaseDirectory(resolution), path + resolution.getSuffix());

		if (imageFile.isFile() && imageFile.canRead()) {
			return imageFile;
		}

		if (resolution == THUMB) {
			File sourceFile = new File(getBaseDirectory(LOW), path + LOW.getSuffix());

			if (!sourceFile.isFile() || !sourceFile.canRead()) {
				return null;
			}

			return createThumbnailImage(sourceFile, imageFile);
		}

		return null;
	}

	public File findImageFile(Facsimile facsimile, FacsimileResolution resolution) {
		return findImageFile(facsimile.getImagePath(), resolution);
	}

	private File createThumbnailImage(File source, final File thumbnailFile) {
		if (!thumbnailFile.getParentFile().isDirectory()) {
			thumbnailFile.getParentFile().mkdirs();
			if (!thumbnailFile.getParentFile().isDirectory()) {
				throw ErrorUtil.fatal("Cannot create directory for thumbnail '" + thumbnailFile.getAbsolutePath()
						+ "'");
			}
		}

		try {
			final Process convertProcess = new ProcessBuilder(conversionTool, source.getAbsolutePath(),
					"-resize", thumbnailWidth + "x" + thumbnailHeight, "-").start();
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
			ErrorUtil.fatal(ioe, "I/O error while generating thumbnail '%s'", thumbnailFile.getAbsolutePath());
		}
		thumbnailFile.delete();
		return null;
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

	public String getRelativePath(File imageFile, FacsimileResolution resolution) {
		String imageFilePath = FilenameUtils.separatorsToUnix(imageFile.getAbsolutePath());
		String basePath = getBaseDirectory(resolution).getAbsolutePath();
		Assert.isTrue(imageFilePath.startsWith(basePath));

		return StringUtils.removeStart(basePath + "/", imageFilePath);
	}
}
