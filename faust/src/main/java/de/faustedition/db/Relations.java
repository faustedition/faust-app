package de.faustedition.db;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.jolbox.bonecp.BoneCPDataSource;
import org.h2.Driver;
import org.h2.tools.Console;
import org.jooq.SQLDialect;
import org.jooq.impl.Factory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Relations {

    private static final Logger LOG = Logger.getLogger(Relations.class.getName());

    private static final String DRIVER_CLASS_NAME =  Driver.class.getName();

    private static final String DB_USER = "sa";

    private static final String DB_PASSWORD = "";


    public static abstract class Transaction<T> {

        public abstract T execute(Factory db) throws Exception;

        public boolean rollsBackOn(Exception e) {
            return true;
        }

        public boolean isReadOnly() {
            return false;
        }

        public int getTransactionIsolation() {
            return Connection.TRANSACTION_READ_COMMITTED;
        }
    }

    public static <T> T execute(DataSource ds, final Transaction<T> tx) {
        Stopwatch sw = null;
        Connection connection = null;
        try {
            try {
                if (LOG.isLoggable(Level.FINE)) {
                    sw = new Stopwatch();
                    sw.start();
                }

                connection = ds.getConnection();
                connection.setReadOnly(tx.isReadOnly());
                connection.setTransactionIsolation(tx.getTransactionIsolation());
                connection.setAutoCommit(false);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Opened connection for {0}", tx);
                }

                final T result = tx.execute(new Factory(connection, SQLDialect.H2));

                connection.commit();
                if (LOG.isLoggable(Level.FINE)) {
                    sw.stop();
                    LOG.log(Level.FINE, "Committed transaction for {0} after {1}", new Object[] { tx, sw });
                }

                return result;
            } catch (Exception e) {
                if (connection != null && tx.rollsBackOn(e)) {
                    connection.rollback();
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Rolled back transaction for " + tx, e);
                    }
                }
                throw e;
            } finally {
                if (connection != null) {
                    connection.close();
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Closed connection for {0}", tx);
                    }
                }
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static DataSource createDataSource(File path) {
        final BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass(DRIVER_CLASS_NAME);
        dataSource.setJdbcUrl(jdbcUrl(path));
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setMinConnectionsPerPartition(1);
        dataSource.setMaxConnectionsPerPartition(20);
        dataSource.setReleaseHelperThreads(0);
        dataSource.setDisableConnectionTracking(true);
        return dataSource;
    }

    public static void console(File path) throws SQLException {
        Console.main(
                "-driver", DRIVER_CLASS_NAME,
                "-url", jdbcUrl(path),
                "-user", DB_USER,
                "-password", DB_PASSWORD
        );
    }

    protected static String jdbcUrl(File path) {
        return path.toURI().toString().replaceAll("^file:", "jdbc:h2://") + ";LOCK_TIMEOUT=30000";
    }

}
