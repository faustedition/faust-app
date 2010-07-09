package de.faustedition.db;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseShutdownHook implements DisposableBean {

	@Autowired
	private SimpleJdbcTemplate jt;

	@Override
	public void destroy() throws Exception {
		jt.update("SHUTDOWN");
	}

}
