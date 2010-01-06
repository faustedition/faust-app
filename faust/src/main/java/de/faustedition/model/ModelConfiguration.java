package de.faustedition.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import de.faustedition.model.facsimile.FacsimileImageDao;
import de.faustedition.model.report.ReportSender;
import de.faustedition.model.repository.RepositoryObject;
import de.faustedition.model.repository.RepositoryUtil;
import de.faustedition.model.security.HasRoleTemplateMethod;
import de.faustedition.util.ResourceUtil;
import de.faustedition.web.URLPathEncoder;
import de.faustedition.web.document.Tei2XhtmlTransformer;

@Configuration
public class ModelConfiguration {
	@Bean
	@Qualifier("data")
	public File dataDirectory() throws IOException {
		Resource resource = ResourceUtil.chooseExistingResource(new Resource[] { new FileSystemResource("/data/faust"),
				new FileSystemResource("/Users/gregor/Documents/Faustedition/data"),
				new FileSystemResource("/home/moz/faust-data") });
		if (resource != null && resource.getFile() != null && resource.getFile().isDirectory()) {
			return resource.getFile();
		} else {
			throw new IllegalStateException("Non of the data directories specified exists on this system.");
		}
	}

	protected File dataSubDirectory(String name) throws IOException {
		File subDirectory = new File(dataDirectory(), name);
		if (subDirectory.isDirectory() || subDirectory.mkdirs()) {
			return subDirectory;
		}
		throw new IllegalStateException("Cannot create data subdirectory '" + subDirectory.getAbsolutePath() + "'");
		
	}
	
	@Bean
	@Qualifier("repository")
	public File repositoryDirectory() throws IOException {
		return dataSubDirectory("repository");
	}

	@Bean
	@Qualifier("backup")
	public File backupDirectory() throws IOException {
		return dataSubDirectory("repository-backup");
	}
	
	@Bean(destroyMethod = "shutdown")
	public JackrabbitRepository repository() throws IOException, RepositoryException {
		JackrabbitRepository repository = RepositoryImpl.create(RepositoryConfig.install(repositoryDirectory()));
		Session session = null;
		try {
			RepositoryUtil.registerNamespace(session = RepositoryUtil.login(repository));
			RepositoryObject.registerNodeTypes(session.getWorkspace().getNodeTypeManager());
		} finally {
			RepositoryUtil.logoutQuietly(session);
		}

		return repository;
	}

	@Bean
	public FacsimileImageDao facsimileImageDao() {
		FacsimileImageDao facsimileImageDao = new FacsimileImageDao();
		facsimileImageDao.setConversionTools(new String[] { "/usr/bin/convert", "/usr/local/bin/convert",
				"/opt/local/bin/convert" });
		return facsimileImageDao;
	}

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(org.postgresql.Driver.class.getName());
		dataSource.setUrl("jdbc:postgresql://localhost/faustedition");
		dataSource.setUsername("faustedition");
		dataSource.setPassword("faustedition");
		return dataSource;
	}

	@Bean
	public SessionFactory sessionFactory() throws Exception {
		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource());
		sessionFactoryBean.setMappingResources(new String[] { "/faust.hbm.xml" });

		Properties hibernateProperties = new Properties();
		hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
		sessionFactoryBean.setHibernateProperties(hibernateProperties);

		sessionFactoryBean.afterPropertiesSet();

		return sessionFactoryBean.getObject();
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		return new HibernateTransactionManager(sessionFactory());
	}

	@Bean
	public JavaMailSender mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setDefaultEncoding("UTF-8");
		mailSender.setHost("localhost");
		return mailSender;
	}

	@Bean
	public ReportSender reportSender() {
		return new ReportSender("Faust-Edition <noreply@faustedition.net>", "Gregor Middell <gregor@middell.net>");
	}

	@Bean
	public FreeMarkerConfigurer freemarkerConfigurer() {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPath("/WEB-INF/freemarker");

		Properties settings = new Properties();
		settings.put("auto_include", "/header.ftl");
		settings.put("default_encoding", "UTF-8");
		settings.put("output_encoding", "UTF-8");
		settings.put("url_escaping_charset", "UTF-8");
		settings.put("strict_syntax", "true");
		settings.put("whitespace_stripping", "true");
		configurer.setFreemarkerSettings(settings);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("hasRole", new HasRoleTemplateMethod());
		variables.put("encodePath", new URLPathEncoder());
		variables.put("tei2xhtml", new Tei2XhtmlTransformer());

		configurer.setFreemarkerVariables(variables);
		return configurer;
	}
}
