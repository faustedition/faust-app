package de.faustedition.util;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

public class DatabaseUtil {
	public static boolean tableExists(final DataSource dataSource, final String tableName) throws MetaDataAccessException {
		return (Boolean) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {

			public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
				boolean tableFound = false;
				ResultSet tables = dbmd.getTables(null, null, tableName, null);
				try {
					while (tables.next()) {
						if (tableName.equalsIgnoreCase(tables.getString("TABLE_NAME"))) {
							tableFound = true;
						}
					}
				} finally {
					tables.close();
				}
				return tableFound;
			}
		});
	}
}
