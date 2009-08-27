package de.faustedition.model.search;

import org.compass.core.Compass;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassHit;
import org.compass.core.CompassTemplate;
import org.compass.core.Resource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.util.LoggingUtil;

public class MetadataIndexerFunctions extends AbstractModelContextTest {

	@Autowired
	private MetadataIndexer indexer;

	private CompassTemplate compassTemplate;

	@Autowired
	public void setCompass(Compass compass) {
		this.compassTemplate = new CompassTemplate(compass);
	}

	@Test
	public void runIndexer() {
		indexer.index();
	}

	public void queryMetadataIndex() {
		CompassDetachedHits hits = compassTemplate.findWithDetach("schema");
		for (int hc = 0; hc < hits.length(); hc++) {
			CompassHit hit = hits.hit(hc);
			Resource resource = hit.resource();
			LoggingUtil.LOG.info(String.format("%d %%: %s (%s)", Math.round(hit.getScore() * 100), resource.getValue("path"), resource.getValue("repositoryType")));
		}
	}
}
