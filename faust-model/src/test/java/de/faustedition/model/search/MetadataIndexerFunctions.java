package de.faustedition.model.search;

import org.compass.core.Compass;
import org.compass.core.CompassHit;
import org.compass.core.Resource;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.util.LoggingUtil;

public class MetadataIndexerFunctions extends AbstractModelContextTest {

	@Autowired
	private MetadataIndexer indexer;

	private CompassSearchHelper compassSearchHelper;
	@Autowired
	public void setCompass(Compass compass) {
		this.compassSearchHelper = new CompassSearchHelper(compass);
	}

	public void runIndexer() {
		indexer.index();
	}

	@Test
	public void queryMetadataIndex() {
		for (CompassHit hit : compassSearchHelper.search(new CompassSearchCommand("schema")).getHits()) {
			Resource resource = hit.resource();
			LoggingUtil.LOG.info(String.format("%d %%: %s (%s)", Math.round(hit.getScore() * 100), resource.getValue("path"), resource.getValue("repositoryType")));
		}
	}
}
