package de.faustedition.model.search;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

public interface SearchIndexCallback
{
	void write(IndexWriter indexWriter) throws SearchException, IOException;

	void queryResult(IndexSearcher searcher, TopDocs documents) throws SearchException, IOException;

	Filter getSearchFilter();

	int getMaxHits();

}
