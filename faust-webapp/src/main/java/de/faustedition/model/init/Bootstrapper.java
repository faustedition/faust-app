package de.faustedition.model.init;

import static de.faustedition.model.tei.TEIDocument.teiElementNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.manuscript.Facsimile;
import de.faustedition.model.manuscript.FacsimileImageDao;
import de.faustedition.model.manuscript.FacsimileImageResolution;
import de.faustedition.model.manuscript.Manuscript;
import de.faustedition.model.manuscript.Portfolio;
import de.faustedition.model.manuscript.Repository;
import de.faustedition.model.manuscript.Transcription;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

public class Bootstrapper
{
	private static final FacsimileImageResolution BOOTSTRAP_RESOLUTION = FacsimileImageResolution.HIGH;

	@Autowired
	private SessionFactory dbSessionFactory;

	@Autowired
	private FacsimileImageDao facsimileImageDao;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ScheduledExecutorService scheduledExecutorService;

	private final List<BootstrapPostProcessor> postProcessors;

	public Bootstrapper()
	{
		this(new ArrayList<BootstrapPostProcessor>());
	}

	public Bootstrapper(List<BootstrapPostProcessor> postProcessors)
	{
		this.postProcessors = postProcessors;
	}

	@PostConstruct
	public void init() throws Exception
	{
		scheduledExecutorService.execute(new Runnable()
		{

			@Override
			public void run()
			{
				bootstrap();
			}
		});
	}

	private void bootstrap()
	{
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult()
		{

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status)
			{
				Session session = dbSessionFactory.getCurrentSession();
				if (!Repository.find(session).isEmpty())
				{
					LoggingUtil.LOG.info("Database contains repositories; skip bootstrapping");
					return;
				}

				Repository repository = null;
				Portfolio portfolio = null;
				LoggingUtil.LOG.info("Bootstrapping database");
				for (File facsimileImageFile : facsimileImageDao.findImageFiles(BOOTSTRAP_RESOLUTION))
				{
					String[] imageFilePath = StringUtils.split(facsimileImageFile.getAbsolutePath(), File.separator);
					if (imageFilePath.length < 3)
					{
						continue;
					}

					String repositoryName = imageFilePath[imageFilePath.length - 3];
					if (repository == null || !repository.getName().equals(repositoryName))
					{
						LoggingUtil.LOG.info("Repository " + repositoryName);
						repository = Repository.findOrCreate(session, repositoryName);
					}

					String portfolioName = imageFilePath[imageFilePath.length - 2];
					if (portfolio == null || !portfolio.getName().equals(portfolioName) || !portfolio.getRepository().equals(repository))
					{
						portfolio = Portfolio.findOrCreate(session, repository, portfolioName);
					}

					String manuscriptFullName = StringUtils.removeEnd(imageFilePath[imageFilePath.length - 1], BOOTSTRAP_RESOLUTION.getSuffix());
					String manuscriptShortName = StringUtils.strip(StringUtils.removeStart(manuscriptFullName, portfolioName), "_-:");
					LoggingUtil.LOG.info("Manuscript " + portfolio.getName() + "_" + manuscriptShortName);

					Manuscript manuscript = Manuscript.findOrCreate(session, portfolio, manuscriptShortName);
					Facsimile facsimile = Facsimile.findOrCreate(session, manuscript, manuscriptShortName, StringUtils.join(new String[] { repositoryName, portfolioName,
							manuscriptFullName }, "/"));
					Transcription transcription = Transcription.find(session, facsimile);
					if (transcription == null)
					{
						transcription = new Transcription();
						transcription.setFacsimile(facsimile);
						transcription.setTextData(XMLUtil.serialize(teiElementNode("text", teiElementNode("body", teiElementNode("p"))).toDOM(), false));
						transcription.setRevisionData(XMLUtil.serialize(teiElementNode("revisionDesc").toDOM(), false));
						session.save(transcription);					}
				}

				try
				{
					for (BootstrapPostProcessor postProcessor : postProcessors)
					{
						LoggingUtil.LOG.info("Running boostrap post-processor " + postProcessor);
						postProcessor.afterBootstrapping();
					}
				}
				catch (Exception e)
				{
					throw ErrorUtil.fatal(e, "Fatal error bootstrapping database");
				}
			}
		});
	}
}
