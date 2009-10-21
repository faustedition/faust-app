package de.faustedition.web.search;

import java.util.List;

import org.compass.core.Compass;
import org.compass.core.CompassHit;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;

import com.google.common.collect.Lists;

import de.faustedition.model.manuscript.Transcription;
import de.faustedition.model.metadata.MetadataAssignment;

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

	public List<SearchResult> execute(Compass compass)
	{
		List<SearchResult> resultList = Lists.newArrayList();
		for (CompassHit hit : new CompassSearchHelper(compass).search(new CompassSearchCommand(query)).getHits())
		{
			Object hitObject = hit.data();
			if (hitObject instanceof MetadataAssignment)
			{
				MetadataAssignment assignment = (MetadataAssignment) hitObject;
				resultList.add(new SearchResult(assignment.getField() + " ==> " + assignment.getValue()));
			}
			else if (hitObject instanceof Transcription)
			{
				resultList.add(new SearchResult("Transcription #" + ((Transcription) hitObject).getId()));
			}
			else
			{
				resultList.add(new SearchResult("\u00a0"));
			}

		}
		return resultList;
	}
}
