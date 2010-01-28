package de.faustedition.model.db;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DatabaseConfiguration {

	@Value("#{config['db.driver']}")
	private String databaseDriver;

	@Value("#{config['db.dialect']}")
	private String databaseDialect;

	@Value("#{config['db.url']}")
	private String databaseUrl;

	@Value("#{config['db.user']}")
	private String databaseUser;

	@Value("#{config['db.password']}")
	private String databasePassword;

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(databaseDriver);
		dataSource.setUrl(databaseUrl);
		dataSource.setUsername(databaseUser);
		dataSource.setPassword(databasePassword);
		return dataSource;
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() {
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource("/db-schema.sql"));
		databasePopulator.setContinueOnError(true);

		DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setDataSource(dataSource());
		initializer.setDatabasePopulator(databasePopulator);
		return initializer;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SimpleJdbcTemplate jdbcTemplate() {
		return new SimpleJdbcTemplate(dataSource());
	}

	@Bean
	public SessionFactory sessionFactory() throws Exception {
		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource());
		sessionFactoryBean.setMappingResources(new String[] { "/faust.hbm.xml" });

		Properties hibernateProperties = new Properties();
		hibernateProperties.put("hibernate.dialect", databaseDialect);
		hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
		sessionFactoryBean.setHibernateProperties(hibernateProperties);

		sessionFactoryBean.afterPropertiesSet();

		return sessionFactoryBean.getObject();
	}

	@Bean
	public PlatformTransactionManager hibernateTransactionManager() throws Exception {
		return new HibernateTransactionManager(sessionFactory());
	}
}
