package de.faustedition.model.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import de.faustedition.util.ErrorUtil;

public class SearchIndex implements InitializingBean
{
	private static final String SEARCH_INDEX_DIRECTORY = "search-index";

	public static final String DEFAULT_FIELD = "default";

	@Autowired
	@Qualifier("dataDirectory")
	private File dataDirectory;

	private String indexName;

	private File indexDirectory;
	private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);

	@Required
	public void setIndexName(String indexName)
	{
		this.indexName = indexName;
	}

	public void setAnalyzer(Analyzer analyzer)
	{
		this.analyzer = analyzer;
	}

	public void writeToIndex(SearchIndexCallback callback) throws SearchException
	{
		IndexWriter indexWriter = null;
		try
		{
			indexWriter = new IndexWriter(FSDirectory.open(indexDirectory), analyzer, MaxFieldLength.LIMITED);
			callback.write(indexWriter);
			indexWriter.commit();
		}
		catch (IOException e)
		{
			throw new SearchException("I/O error while writing to index '" + indexName + "'", e);
		}
		finally
		{
			if (indexWriter != null)
			{
				try
				{
					indexWriter.rollback();
				}
				catch (IOException e)
				{
					throw new SearchException("I/O error while closing writer to index '" + indexName + "'", e);
				}
			}
		}
	}

	public void query(String query, SearchIndexCallback callback) throws SearchException
	{
		IndexSearcher indexSearcher = null;
		try
		{
			indexSearcher = new IndexSearcher(FSDirectory.open(indexDirectory), true);
			callback.queryResult(indexSearcher, indexSearcher.search(new QueryParser(SearchIndex.DEFAULT_FIELD, analyzer).parse(query), callback.getSearchFilter(), callback.getMaxHits()));
		}
		catch (IOException e)
		{
			throw new SearchException(String.format("I/O error while querying search index '%s' with query '%s", indexName, query), e);
		}
		catch (ParseException e)
		{
			throw new SearchException(String.format("Parser error while querying search index '%s' with query '%s", indexName, query), e);
		}
		finally
		{
			if (indexSearcher != null)
			{
				try
				{
					indexSearcher.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		indexDirectory = new File(new File(dataDirectory, SEARCH_INDEX_DIRECTORY), indexName);
		indexDirectory.mkdirs();

		if (!indexDirectory.isDirectory())
		{
			throw ErrorUtil.fatal("Cannot access search index directory '%s'", indexDirectory.getAbsolutePath());
		}
	}

}
