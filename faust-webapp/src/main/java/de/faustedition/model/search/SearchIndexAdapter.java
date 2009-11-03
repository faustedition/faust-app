package de.faustedition.model.search;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

public class SearchIndexAdapter implements SearchIndexCallback
{

	@Override
	public int getMaxHits()
	{
		return 1000;
	}

	@Override
	public Filter getSearchFilter()
	{
		return null;
	}

	@Override
	public void queryResult(IndexSearcher searcher, TopDocs documents) throws SearchException, IOException
	{
	}

	@Override
	public void write(IndexWriter indexWriter) throws SearchException, IOException
	{
	}
}
