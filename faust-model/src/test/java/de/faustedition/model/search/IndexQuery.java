package de.faustedition.model.search;

import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassException;
import org.compass.core.CompassHitsOperations;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.util.LoggingUtil;

public class IndexQuery extends AbstractModelContextTest
{
	@Autowired
	private Compass compass;

	@Test
	public void queryIndex() throws Exception
	{
		CompassHitsOperations hits = new CompassTemplate(compass).executeFind(new CompassCallback<CompassHitsOperations>()
		{

			@Override
			public CompassHitsOperations doInCompass(CompassSession session) throws CompassException
			{
				return session.find("gsa");
			}
		});

		LoggingUtil.LOG.info("GSA hits: " + hits.length());
	}
}
