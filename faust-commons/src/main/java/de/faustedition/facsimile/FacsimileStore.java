package de.faustedition.facsimile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.faustedition.Log;

@Service
public class FacsimileStore implements InitializingBean, Iterable<Facsimile> {
	public static final String TIF_EXTENSION = ".tif";

	@Value("#{config['facsimile.home']}")
	private String baseDirectory;

	@Value("#{config['facsimile.identify']}")
	private String identifyCommand;

	@Value("#{config['facsimile.convert_ptif']}")
	private String convertToPyramidalTiffCommmand;

	private File tifBase;
	private File ptifBase;

	@Override
	public void afterPropertiesSet() throws Exception {
		tifBase = subDirectory("tif");
		ptifBase = subDirectory("ptif");
	}

	public File facsimile(Facsimile facsimile) {
		final File facsimileFile = toFile(facsimile);
		return facsimileFile.isFile() ? facsimileFile : null;
	}

	public FacsimileProperties properties(Facsimile facsimile) {
		File facsimileFile = toFile(facsimile);
		if (!facsimileFile.isFile()) {
			return null;
		}

		String facsimilePath = escapePath(facsimileFile.getAbsolutePath());
		try {
			String identifyCommand = MessageFormat.format(this.identifyCommand, facsimilePath);
			Log.LOGGER.trace("Identifying facsimile: '{}'", identifyCommand);
			Process identifyProcess = Runtime.getRuntime().exec(identifyCommand);

			int exitValue = -1;
			InputStream identifyData = null;
			FacsimileProperties properties = null;
			try {
				properties = FacsimileProperties.fromSpec(IOUtils.toString(identifyData = identifyProcess.getInputStream()));
			} finally {
				IOUtils.closeQuietly(identifyData);
			}

			try {
				exitValue = identifyProcess.waitFor();
			} catch (InterruptedException e) {
			}

			if (exitValue == 0) {
				return properties;
			}

			throw Log.fatalError("Error while identifying '%s': %s", facsimilePath, Integer.toString(exitValue));
		} catch (IOException e) {
			throw Log.fatalError(e, "I/O error while identifying '%s'", facsimilePath);
		}

	}

	public File tiles(Facsimile facsimile) {
		File source = toFile(facsimile);
		File result = new File(ptifBase, facsimile.getPath() + TIF_EXTENSION);

		Assert.isTrue(source.isFile(), source.getAbsolutePath() + " exists");
		Log.LOGGER.trace("Building tiles for {} ==> {}", new Object[] { facsimile, result.getAbsolutePath() });

		if (result.isFile() && result.canRead() && result.length() > 0 && (source.lastModified() <= result.lastModified())) {
			Log.LOGGER.trace("Found up-to-date tiles for {} in {}", new Object[] { facsimile, result.getAbsolutePath() });
			return result;
		}

		File resultDir = result.getParentFile();
		if (!resultDir.isDirectory() && !resultDir.mkdirs()) {
			throw Log.fatalError("Cannot access/create directory '%s'", resultDir.getAbsolutePath());
		}

		String sourcePath = escapePath(source.getAbsolutePath());
		String resultPath = escapePath(result.getAbsolutePath());
		String command = MessageFormat.format(convertToPyramidalTiffCommmand, sourcePath, resultPath);
		try {
			int exitValue = -1;
			try {
				Log.LOGGER.trace("Generating facsimile tiles: '{}'", command);
				Process conversionProcess = Runtime.getRuntime().exec(command);
				exitValue = conversionProcess.waitFor();
			} catch (InterruptedException e) {
			}

			if (exitValue != 0) {
				throw Log.fatalError("Error while converting '%s': %s", sourcePath, Integer.toString(exitValue));
			}
		} catch (IOException e) {
			throw Log.fatalError(e, "I/O error while converting '%s'", sourcePath);
		}

		return result;
	}

	@Override
	public Iterator<Facsimile> iterator() {
		final Iterator<?> facsimiles = FileUtils.listFiles(tifBase, FACSIMILE_FILE_FILTER, TrueFileFilter.INSTANCE).iterator();
		return new Iterator<Facsimile>() {

			@Override
			public boolean hasNext() {
				return facsimiles.hasNext();
			}

			@Override
			public Facsimile next() {
				return toFacsimile((File) facsimiles.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	protected static String escapePath(String path) {
		return path.replaceAll("\\s", "\\\\ ");
	}

	protected Facsimile toFacsimile(File file) {
		String basePath = FilenameUtils.separatorsToUnix(tifBase.getAbsolutePath());
		String currentPath = StringUtils.removeEnd(FilenameUtils.separatorsToUnix(file.getAbsolutePath()), TIF_EXTENSION);
		return new Facsimile(StringUtils.strip(StringUtils.removeStart(currentPath, basePath), "/"));
	}

	protected File toFile(Facsimile facsimile) {
		return new File(tifBase, facsimile.getPath() + TIF_EXTENSION);
	}

	protected File subDirectory(String name) throws IOException {
		File subDirectory = new File(baseDirectory, name);
		if (subDirectory.isDirectory() || subDirectory.mkdirs()) {
			return subDirectory;
		}
		throw new IllegalStateException("Cannot create subdirectory '" + subDirectory.getAbsolutePath() + "'");

	}

	private static final FileFileFilter FACSIMILE_FILE_FILTER = new FileFileFilter() {
		@Override
		public boolean accept(File file) {
			return super.accept(file) && file.getName().endsWith(TIF_EXTENSION);
		}
	};

}
