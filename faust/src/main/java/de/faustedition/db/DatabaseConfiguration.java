package de.faustedition.db;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.transaction.SpringTransactionManager;
import org.neo4j.kernel.impl.transaction.UserTransactionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Configuration
public class DatabaseConfiguration {
	@Autowired
	private Environment environment;

	@Bean
	public DataSource dataSource() throws Exception {
        return Relations.createDataSource(environment.getRequiredProperty("db.home", File.class));
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		final EmbeddedGraphDatabase graphDatabase = graphDatabase();
		return new ChainedTransactionManager(
			new JtaTransactionManager(new UserTransactionImpl(graphDatabase), new SpringTransactionManager(graphDatabase)),
			new DataSourceTransactionManager(dataSource()));
	}

	@Bean
	public TransactionTemplate transactionTemplate() throws Exception {
		return new TransactionTemplate(transactionManager());
	}

	@Bean(destroyMethod = "shutdown")
	public EmbeddedGraphDatabase graphDatabase() throws IOException {
		return new EmbeddedGraphDatabase(environment.getRequiredProperty("graph.home", File.class).getCanonicalPath());
	}
}
