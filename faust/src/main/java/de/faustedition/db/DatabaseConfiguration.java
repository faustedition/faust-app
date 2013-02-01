package de.faustedition.db;

import com.jolbox.bonecp.BoneCPDataSource;
import de.faustedition.transcript.TranscribedVerseInterval;
import de.faustedition.transcript.Transcript;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextTarget;
import org.h2.Driver;
import org.hibernate.SessionFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.transaction.SpringTransactionManager;
import org.neo4j.kernel.impl.transaction.UserTransactionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
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
		final File database = environment.getRequiredProperty("db.home", File.class);

		final BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setDriverClass(Driver.class.getName());
		dataSource.setJdbcUrl(database.toURI().toString().replaceAll("^file:", "jdbc:h2://") + ";LOCK_TIMEOUT=60000");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		dataSource.setMinConnectionsPerPartition(1);
		dataSource.setMaxConnectionsPerPartition(20);
		dataSource.setReleaseHelperThreads(0);
		dataSource.setDisableConnectionTracking(true);
		return dataSource;
	}

	@Bean
	public SessionFactory sessionFactory() throws Exception {
		return new LocalSessionFactoryBuilder(dataSource())
			.addAnnotatedClasses(
				Annotation.class,
				Name.class,
				Text.class,
				TextTarget.class,
				Transcript.class,
				TranscribedVerseInterval.class
			).buildSessionFactory();
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		final EmbeddedGraphDatabase graphDatabase = graphDatabase();
		return new ChainedTransactionManager(
			new JtaTransactionManager(new UserTransactionImpl(graphDatabase), new SpringTransactionManager(graphDatabase)),
			new HibernateTransactionManager(sessionFactory()));
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
