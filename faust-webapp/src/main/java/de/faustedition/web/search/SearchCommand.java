package de.faustedition.web.search;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;
import de.faustedition.model.search.SearchException;
import de.faustedition.model.search.SearchIndex;
import de.faustedition.model.search.SearchIndexAdapter;

public class SearchCommand
{
	private String query;

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public List<SearchResult> execute(SearchIndex searchIndex) throws SearchException
	{
		if (StringUtils.isBlank(query))
		{
			return Collections.emptyList();
		}
		final List<SearchResult> resultList = Lists.newArrayList();
		searchIndex.query(query, new SearchIndexAdapter()
		{
			@Override
			public void queryResult(IndexSearcher searcher, TopDocs documents) throws IOException
			{
				for (ScoreDoc document : documents.scoreDocs)
				{
					Document luceneDocument = searcher.doc(document.doc);
					String clazzName = luceneDocument.get("class");
					if (MetadataAssignment.class.getName().equals(clazzName))
					{
						resultList.add(new SearchResult(String.format("%s ==> %s", luceneDocument.get("field"), luceneDocument.get("value"))));
					}
					else if (Transcription.class.getName().equals(clazzName))
					{
						resultList.add(new SearchResult(String.format("Transcription #%s", luceneDocument.get("id"))));
					}
				}
			}
		});
		return resultList;
	}
}
