package de.faustedition.model.facsimile;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import de.faustedition.util.ErrorUtil;

public class FacsimileManager {
	private static final Logger LOG = LoggerFactory.getLogger(FacsimileManager.class);
	private Map<FacsimileResolution, File> baseDirectories;
	private Map<FacsimileResolution, String> conversionCommands;

	@Required
	public void setBaseDirectories(Map<FacsimileResolution, File> baseDirectories) {
		this.baseDirectories = baseDirectories;
	}

	@Required
	public void setConversionCommands(Map<FacsimileResolution, String> conversionCommands) {
		this.conversionCommands = conversionCommands;
	}

	public File find(String path, FacsimileResolution resolution) {
		LOG.debug("Retrieving facsimile {} ({})", path, resolution);
		File result = file(resolution, path);
		File source = null;

		FacsimileResolution sourceResolution = findSourceResolution(resolution);
		if (sourceResolution != null) {
			source = file(sourceResolution, path);
			if (!source.isFile() || !source.canRead()) {
				source = null;
			}
		}

		if (result.isFile() && result.canRead() && result.length() > 0
				&& (source == null || (source.lastModified() <= result.lastModified()))) {
			LOG.debug("Returning up-to-date facsimile file {}", result);
			return result;
		}

		if (source == null || !conversionCommands.containsKey(resolution)) {
			LOG.debug("No facsimile or no source for facsimile {} ({})", path, resolution);
			return null;
		}

		File resultDir = result.getParentFile();
		if (!resultDir.isDirectory() && !resultDir.mkdirs()) {
			throw ErrorUtil.fatal("Cannot access/create directory '%s'", resultDir.getAbsolutePath());
		}

		String sourcePath = escapePath(source.getAbsolutePath());
		String resultPath = escapePath(result.getAbsolutePath());
		String command = MessageFormat.format(conversionCommands.get(resolution), sourcePath, resultPath);
		try {
			int exitValue = -1;
			try {
				LOG.debug("Generating facsimile: '{}'", command);
				Process conversionProcess = Runtime.getRuntime().exec(command);
				exitValue = conversionProcess.waitFor();
			} catch (InterruptedException e) {
			}

			if (exitValue == 0) {
				LOG.debug("Returning generated facsimile file {}", result);
				return result;
			}

			throw ErrorUtil.fatal("Error while converting '%s': %s", sourcePath, Integer.toString(exitValue));
		} catch (IOException e) {
			throw ErrorUtil.fatal(e, "I/O error while converting '%s'", sourcePath);
		}
	}

	public SortedSet<String> findAllPaths() {
		final SortedSet<String> uris = new TreeSet<String>();
		FacsimileResolution[] resolutions = FacsimileResolution.values();
		for (int rc = resolutions.length - 1; rc >= 0; rc--) {
			final FacsimileResolution res = resolutions[rc];
			for (Object file : FileUtils.listFiles(baseDirectories.get(res), new FileFileFilter() {
				@Override
				public boolean accept(File file) {
					return super.accept(file) && file.getName().endsWith(res.getSuffix());
				}
			}, TrueFileFilter.INSTANCE)) {
				uris.add(uriPath(res, (File) file));
			}
			if (!uris.isEmpty()) {
				return uris;
			}
		}
		return uris;
	}

	public void generateAll() {
		FacsimileResolution[] resolutions = FacsimileResolution.values();
		for (int rc = resolutions.length - 1; rc >= 0; rc--) {
			generateAllFor(resolutions[rc]);
		}
	}

	public void generateAllFor(FacsimileResolution resultResolution) {
		FacsimileResolution sourceResolution = findSourceResolution(resultResolution);
		if (sourceResolution == null) {
			return;
		}

		File baseDirectory = baseDirectories.get(sourceResolution);
		generate(baseDirectory, baseDirectory, sourceResolution, resultResolution);
	}

	private void generate(File base, File current, FacsimileResolution sourceResolution, FacsimileResolution resultResolution) {
		for (File file : current.listFiles()) {
			if (file.isDirectory()) {
				generate(base, file, sourceResolution, resultResolution);
			}
			if (!file.isFile()) {
				continue;
			}
			if (file.getName().endsWith(sourceResolution.getSuffix())) {
				find(uriPath(sourceResolution, file), resultResolution);
			}
		}
	}

	protected String uriPath(FacsimileResolution res, File file) {
		String basePath = FilenameUtils.separatorsToUnix(baseDirectories.get(res).getAbsolutePath());
		String currentPath = StringUtils.removeEnd(FilenameUtils.separatorsToUnix(file.getAbsolutePath()), res.getSuffix());
		return StringUtils.strip(StringUtils.removeStart(currentPath, basePath), "/");
	}

	protected File file(FacsimileResolution resolution, String path) {
		return new File(baseDirectories.get(resolution), path + resolution.getSuffix());
	}

	public static String escapePath(String path) {
		return path.replaceAll("\\s", "\\\\ ");
	}

	private static FacsimileResolution findSourceResolution(FacsimileResolution dest) {
		FacsimileResolution[] resolutions = FacsimileResolution.values();
		for (int rc = 0; rc < (resolutions.length - 1); rc++) {
			if (dest.equals(resolutions[rc])) {
				return resolutions[rc + 1];
			}
		}
		return null;
	}
}
