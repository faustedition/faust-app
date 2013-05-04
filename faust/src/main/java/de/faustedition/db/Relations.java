package de.faustedition.db;

import com.jolbox.bonecp.BoneCPDataSource;
import org.h2.Driver;
import org.h2.tools.Console;
import org.h2.tools.Shell;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Relations {

    private static final String DRIVER_CLASS_NAME =  Driver.class.getName();

    private static final String DB_USER = "sa";

    private static final String DB_PASSWORD = "";

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
