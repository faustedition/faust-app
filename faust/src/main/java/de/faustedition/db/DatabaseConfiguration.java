package de.faustedition.db;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

@Configuration
public class DatabaseConfiguration {

	@Value("#{config['db.home']}")
	private String databaseHome;

	@Bean
	public DataSource dataSource() {
		File db = new File(databaseHome);
		if (!db.isDirectory()) {
			Assert.isTrue(db.mkdirs(), "Could not create database home directory '" + db.getAbsolutePath() + "'");
		}
		return new DriverManagerDataSource("jdbc:hsqldb:file:" + db.getAbsolutePath() + "/faust", "SA", "");
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer() {
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource("db-schema.sql", DatabaseConfiguration.class));
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
	@DependsOn("dataSourceInitializer")
	public SimpleJdbcTemplate jdbcTemplate() {
		return new SimpleJdbcTemplate(dataSource());
	}
}
