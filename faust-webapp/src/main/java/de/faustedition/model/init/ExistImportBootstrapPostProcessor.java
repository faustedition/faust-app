package de.faustedition.model.init;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.practicalxml.ParseUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;

import com.google.common.base.Preconditions;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.manuscript.TranscriptionDocument;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

public class ExistImportBootstrapPostProcessor implements BootstrapPostProcessor, InitializingBean
{

	@Autowired
	@Qualifier("dataDirectory")
	private File dataDirectory;

	@Autowired
	private SessionFactory dbSessionFactory;

	private File importDirectory;

	@Override
	public void afterBootstrapping()
	{
		Session session = dbSessionFactory.getCurrentSession();
		for (File repositoryDir : importDirectory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY))
		{
			Repository repository = Repository.find(session, repositoryDir.getName());
			if (repository == null)
			{
				LoggingUtil.LOG.warn("Could not find repository: " + repositoryDir.getName());
				continue;
			}
			for (File portfolioDir : repositoryDir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY))
			{
				File[] transcriptionFiles = portfolioDir.listFiles((FileFilter) FileFileFilter.FILE);
				if (transcriptionFiles.length == 0)
				{
					continue;
				}

				Portfolio portfolio = Portfolio.find(session, repository, portfolioDir.getName());
				if (portfolio == null)
				{
					LoggingUtil.LOG.warn("Could not find portfolio: " + portfolioDir.getName());
					continue;
				}

				for (File transcriptionFile : transcriptionFiles)
				{
					String manuscriptName = StringUtils.strip(StringUtils.removeEnd(StringUtils.removeStart(transcriptionFile.getName(), portfolio.getName()), ".xml"), "_-:");
					Manuscript manuscript = Manuscript.find(session, portfolio, manuscriptName);
					if (manuscript == null)
					{
						LoggingUtil.LOG.warn("Could not find manuscript: " + transcriptionFile.getName());
						continue;
					}
					FileInputStream transcriptionStream = null;
					try
					{
						TranscriptionDocument transcriptionDocument = new TranscriptionDocument(ParseUtil.parse(new InputSource(transcriptionStream = new FileInputStream(
								transcriptionFile))));
						if (transcriptionDocument.hasText())
						{
							LoggingUtil.LOG.info("eXist ==> " + portfolio.getName() + "_" + manuscript.getName());
							Facsimile facsimile = Facsimile.find(session, manuscript, manuscript.getName());
							Preconditions.checkNotNull(facsimile);

							Transcription transcription = Transcription.find(session, facsimile);
							Preconditions.checkNotNull(transcription);

							transcriptionDocument.update(transcription);
						}
					} catch (IOException e)
					{
						throw ErrorUtil.fatal("I/O error while reading transcription from " + transcriptionFile.getAbsolutePath(), e);
					} finally
					{
						IOUtils.closeQuietly(transcriptionStream);
					}
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		importDirectory = new File(dataDirectory, "eXist-backup");
		Assert.isTrue(importDirectory.isDirectory(), "eXist import directory does not exist");
	}

}
