package de.faustedition.db;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.jolbox.bonecp.BoneCPDataSource;
import org.h2.Driver;
import org.h2.engine.Database;
import org.h2.tools.Console;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        public abstract T execute(DSLContext sql) throws Exception;

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

                final T result = tx.execute(DSL.using(connection, SQLDialect.H2));

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

    public static BoneCPDataSource createDataSource(File path, boolean registerShutdownHook) {
        final BoneCPDataSource dataSource = new BoneCPDataSource();
        dataSource.setDriverClass(DRIVER_CLASS_NAME);
        dataSource.setJdbcUrl(jdbcUrl(path));
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        dataSource.setMinConnectionsPerPartition(1);
        dataSource.setMaxConnectionsPerPartition(20);
        dataSource.setReleaseHelperThreads(0);
        dataSource.setDisableConnectionTracking(true);
        dataSource.setLogStatementsEnabled(Logger.getLogger(BoneCPDataSource.class.getPackage().getName()).isLoggable(Level.FINE));
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Closing database connection pool " + dataSource);
                    }
                    dataSource.close();
                }
            }));
        }
        return dataSource;
    }

    public static DataSource init(DataSource dataSource) throws IOException, SQLException {
        final Closer closer = Closer.create();
        try {
            restore(dataSource, closer.register(new InputStreamReader(
                    Relations.class.getResourceAsStream("/db-schema.sql"),
                    Charset.forName("UTF-8")
            )));
            return dataSource;
        } finally {
            closer.close();
        }
    }

    public static void backup(DataSource dataSource, Writer to) throws SQLException, IOException {
        Connection connection = null;
        PreparedStatement script = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            script = connection.prepareStatement("SCRIPT DROP BLOCKSIZE 10485760");
            resultSet = script.executeQuery();
            while (resultSet.next()) {
                final Reader scriptReader = resultSet.getCharacterStream(1);
                try {
                    CharStreams.copy(scriptReader, to);
                } finally {
                    Closeables.close(scriptReader, false);
                }
                to.write("\n");
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (script != null) {
                script.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static void restore(DataSource dataSource, File from, Charset charset) throws SQLException {
        Connection connection = null;
        Statement runScript = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            runScript = connection.createStatement();
            runScript.executeUpdate(String.format("RUNSCRIPT FROM '%s' CHARSET '%s'", from.getPath(), charset.name()));
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (runScript != null) {
                runScript.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    public static void restore(DataSource dataSource, Reader from) throws IOException, SQLException {
        final File restoreSql = File.createTempFile(Relations.class.getName() + ".restore", ".sql");
        restoreSql.deleteOnExit();

        try {
            final Charset charset = Charset.forName("UTF-8");
            Writer tempWriter = null;
            try {
                CharStreams.copy(from, tempWriter = new OutputStreamWriter(new FileOutputStream(restoreSql), charset));
            } finally {
                Closeables.close(tempWriter, false);
            }
            restore(dataSource, restoreSql, charset);
        } finally {
            restoreSql.delete();
        }
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
        return new File(path, "relations").toURI().toString().replaceAll("^file:", "jdbc:h2://");
    }

}
