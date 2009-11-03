package de.faustedition.model.manuscript;

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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import de.faustedition.util.ErrorUtil;

public class FacsimileImageDao implements InitializingBean
{
	@Autowired
	@Qualifier("dataDirectory")
	private File dataDirectory;

	private int thumbnailHeight = 150;
	private int thumbnailWidth = 75;
	private String[] conversionTools;

	private String imageMagickConvertCommand;
	private File lowResolutionImageDirectory;
	private File highResolutionImageDirectory;
	private File thumbnailDirectory;

	@Required
	public void setConversionTools(String[] conversionTools)
	{
		this.conversionTools = conversionTools;
	}

	public void setThumbnailHeight(int thumbnailHeight)
	{
		this.thumbnailHeight = thumbnailHeight;
	}

	public void setThumbnailWidth(int thumbnailWidth)
	{
		this.thumbnailWidth = thumbnailWidth;
	}

	@SuppressWarnings("unchecked")
	public SortedSet<File> findImageFiles(final FacsimileImageResolution resolution)
	{
		SortedSet<File> imageFileSet = new TreeSet<File>(new Comparator<File>()
		{

			@Override
			public int compare(File o1, File o2)
			{
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
		});
		imageFileSet.addAll(FileUtils.listFiles(getBaseDirectory(resolution), new AbstractFileFilter()
		{

			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(resolution.getSuffix());
			}

		}, TrueFileFilter.INSTANCE));
		return imageFileSet;
	}

	public File findImageFile(Facsimile facsimile)
	{
		return findImageFile(facsimile, FacsimileImageResolution.LOW);
	}

	public File findImageFile(Facsimile facsimile, FacsimileImageResolution resolution)
	{
		final File imageFile = new File(getBaseDirectory(resolution), facsimile.getImagePath() + resolution.getSuffix());

		if (imageFile.isFile() && imageFile.canRead())
		{
			return imageFile;
		}

		if (resolution == FacsimileImageResolution.THUMB)
		{
			File sourceFile = new File(getBaseDirectory(FacsimileImageResolution.LOW), facsimile.getImagePath() + FacsimileImageResolution.LOW.getSuffix());

			if (!sourceFile.isFile() || !sourceFile.canRead())
			{
				return null;
			}

			return createThumbnailImage(sourceFile, imageFile);
		}

		return null;
	}

	private File createThumbnailImage(File source, final File thumbnailFile)
	{
		if (!thumbnailFile.getParentFile().isDirectory())
		{
			thumbnailFile.getParentFile().mkdirs();
			if (!thumbnailFile.getParentFile().isDirectory())
			{
				throw ErrorUtil.fatal("Cannot create directory for thumbnail '" + thumbnailFile.getAbsolutePath() + "'");
			}
		}

		try
		{
			final Process convertProcess = new ProcessBuilder(imageMagickConvertCommand, source.getAbsolutePath(), "-resize", thumbnailWidth + "x" + thumbnailHeight, "-").start();
			Thread conversionResultReaderThread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					InputStream convertOutput = null;
					OutputStream thumbnailStream = null;
					try
					{
						convertOutput = convertProcess.getInputStream();
						thumbnailStream = new FileOutputStream(thumbnailFile);
						IOUtils.copy(convertOutput, thumbnailStream);
					}
					catch (IOException e)
					{
						IOUtils.closeQuietly(thumbnailStream);
						thumbnailFile.delete();
					}
					finally
					{
						IOUtils.closeQuietly(thumbnailStream);
						IOUtils.closeQuietly(convertOutput);
					}
				}
			});
			conversionResultReaderThread.setDaemon(true);
			conversionResultReaderThread.start();

			int conversionResult = -1;
			try
			{
				conversionResult = convertProcess.waitFor();
			}
			catch (InterruptedException e)
			{
			}

			try
			{
				conversionResultReaderThread.join();
			}
			catch (InterruptedException ie)
			{
			}

			if (conversionResult == 0)
			{
				return thumbnailFile;
			}
		}
		catch (IOException ioe)
		{
			ErrorUtil.fatal(ioe, "I/O error while generating thumbnail '%s'", thumbnailFile.getAbsolutePath());
		}
		thumbnailFile.delete();
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
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

		for (String conversionTool : conversionTools)
		{
			File conversionToolFile = new File(conversionTool);
			if (conversionToolFile.isFile())
			{
				imageMagickConvertCommand = conversionToolFile.getAbsolutePath();
			}
		}
		Assert.notNull(imageMagickConvertCommand, "No ImageMagick 'convert' command could by found on this system");
	}

	protected File getBaseDirectory(FacsimileImageResolution resolution)
	{
		switch (resolution)
		{
		case LOW:
			return lowResolutionImageDirectory;
		case HIGH:
			return highResolutionImageDirectory;
		case THUMB:
			return thumbnailDirectory;
		}

		throw new IllegalArgumentException(resolution.toString());
	}

	public String getRelativePath(File imageFile, FacsimileImageResolution resolution)
	{
		String imageFilePath = FilenameUtils.separatorsToUnix(imageFile.getAbsolutePath());
		String basePath = getBaseDirectory(resolution).getAbsolutePath();
		Assert.isTrue(imageFilePath.startsWith(basePath));

		return StringUtils.removeStart(basePath + "/", imageFilePath);
	}
}
