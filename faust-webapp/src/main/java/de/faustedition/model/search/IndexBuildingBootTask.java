package de.faustedition.model.search;

import java.util.concurrent.Executors;

import org.compass.gps.CompassGps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class IndexBuildingBootTask implements Runnable, InitializingBean
{

	@Autowired
	CompassGps compassGps;

	@Override
	public void run()
	{
		compassGps.index();
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Executors.newSingleThreadExecutor().execute(this);
	}

}
