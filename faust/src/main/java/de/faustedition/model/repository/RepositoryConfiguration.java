package de.faustedition.model.repository;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.apache.commons.lang.ArrayUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.faustedition.model.DatastoreConfiguration;

@Configuration
public class RepositoryConfiguration {
	@Autowired
	private DatastoreConfiguration datastoreConfiguration;
	
	@Bean
	@Qualifier("repository")
	public File repositoryDirectory() throws IOException {
		return datastoreConfiguration.dataSubDirectory("repository");
	}

	@Bean
	@Qualifier("backup")
	public File backupDirectory() throws IOException {
		return datastoreConfiguration.dataSubDirectory("repository-backup");
	}

	@Bean(destroyMethod = "shutdown")
	public JackrabbitRepository repository() throws IOException, RepositoryException {
		JackrabbitRepository repository = RepositoryImpl.create(RepositoryConfig.install(repositoryDirectory()));
		Session session = null;
		try {
			session = repository.login(RepositoryUtil.DEFAULT_CREDENTIALS);

			Workspace ws = session.getWorkspace();
			String[] accessibleWorkspaceNames = ws.getAccessibleWorkspaceNames();
			if (!ArrayUtils.contains(accessibleWorkspaceNames, RepositoryUtil.XML_WS)) {
				ws.createWorkspace(RepositoryUtil.XML_WS);
			}
			
			return repository;
		} finally {
			RepositoryUtil.logoutQuietly(session);
		}
	}
}
