package de.faustedition.model.search;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.index.IndexWriter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.HibernateUtil;
import de.faustedition.util.LoggingUtil;

@Service
public class SearchIndexBuildTask implements Runnable
{
	@Autowired
	private SearchIndex searchIndex;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	
	@Override
	public void run()
	{
		try
		{
			LoggingUtil.LOG.info("Indexing data repository ...");
			StopWatch stopWatch = new StopWatch();
			
			stopWatch.start();
			searchIndex.writeToIndex(new SearchIndexAdapter()
			{

				@Override
				public void write(IndexWriter indexWriter) throws SearchException, IOException
				{
					indexWriter.deleteAll();
					doIndex(indexWriter);
				}
			});
			stopWatch.stop();
			
			LoggingUtil.LOG.info("Indexed data repository in " + stopWatch);
		}
		catch (SearchException e)
		{
			throw ErrorUtil.fatal(e, "Error while building search index");
		}
	}

	private void doIndex(final IndexWriter indexWriter) throws IOException
	{
		try
		{
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setReadOnly(true);
			transactionTemplate.execute(new TransactionCallbackWithoutResult()
			{

				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status)
				{
					try
					{
						Session session = sessionFactory.getCurrentSession();
						for (Transcription t : HibernateUtil.scroll(session.createCriteria(Transcription.class), Transcription.class))
						{
							indexWriter.addDocument(t.getLuceneDocument());
						}
						for (MetadataAssignment m : HibernateUtil.scroll(session.createCriteria(MetadataAssignment.class), MetadataAssignment.class))
						{
							indexWriter.addDocument(m.getLuceneDocument());
						}
					}
					catch (IOException e)
					{
						throw ErrorUtil.fatal(e, "I/O error while indexing");
					}
				}
			});
		}
		catch (TransactionException e)
		{
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			if (rootCause instanceof IOException)
			{
				throw (IOException) rootCause;
			}
		}
	}

	@PostConstruct
	public void init() throws Exception
	{
		scheduledExecutorService.execute(this);
	}
}
