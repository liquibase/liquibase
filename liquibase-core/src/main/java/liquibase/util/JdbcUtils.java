package liquibase.util;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Column;

import java.sql.*;
import java.util.Collection;

public abstract class JdbcUtils {

    /**
     * Constant that indicates an unknown (or unspecified) SQL type.
     *
     * @see java.sql.Types
     */
    public static final int TYPE_UNKNOWN = Integer.MIN_VALUE;

    /**
     * Close the given JDBC Statement and ignore any thrown exception.
     * This is useful for typical finally blocks in manual JDBC code.
     *
     * @param stmt the JDBC Statement to close (may be <code>null</code>)
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
//                logger.debug("Unexpected exception on closing JDBC Statement", ex);
            }
        }
    }

    /**
     * Close the given JDBC ResultSet and ignore any thrown exception.
     * This is useful for typical finally blocks in manual JDBC code.
     *
     * @param rs the JDBC ResultSet to close (may be <code>null</code>)
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
//                logger.debug("Unexpected exception on closing JDBC ResultSet", ex);
            }
        }
    }

    public static void close(ResultSet rs, Statement stmt) {
        closeResultSet(rs);
        closeStatement(stmt);
    }

    /**
     * Retrieve a JDBC column value from a ResultSet, using the most appropriate
     * value type. The returned value should be a detached value object, not having
     * any ties to the active ResultSet: in particular, it should not be a Blob or
     * Clob object but rather a byte array respectively String representation.
     * <p>Uses the <code>getObject(index)</code> method, but includes additional "hacks"
     * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
     * datatype and a <code>java.sql.Date</code> for DATE columns leaving out the
     * time portion: These columns will explicitly be extracted as standard
     * <code>java.sql.Timestamp</code> object.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     * @see java.sql.Blob
     * @see java.sql.Clob
     * @see java.sql.Timestamp
     */
    public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
        Object obj = null;
        try {
            obj = rs.getObject(index);
        } catch (SQLException e) {
            if ("The conversion from char to SMALLINT is unsupported.".equals(e.getMessage())) {
                //issue with sqlserver jdbc 3.0 http://social.msdn.microsoft.com/Forums/sqlserver/en-US/2c908b45-6f75-484a-a891-5e8206f8844f/conversion-error-in-the-jdbc-30-driver-when-accessing-metadata
                obj = rs.getString(index);
            } else {
                throw e;
            }
        }
        if (obj instanceof Blob) {
            obj = rs.getBytes(index);
        } else if (obj instanceof Clob) {
            obj = rs.getString(index);
        } else if ((obj != null) && obj.getClass().getName().startsWith("oracle.sql.TIMESTAMP")) {
            obj = rs.getTimestamp(index);
        } else if ((obj != null) && obj.getClass().getName().startsWith("oracle.sql.DATE")) {
            String metaDataClassName = rs.getMetaData().getColumnClassName(index);
            if ("java.sql.Timestamp".equals(metaDataClassName) ||
                    "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
                obj = rs.getTimestamp(index);
            } else {
                obj = rs.getDate(index);
            }
        } else if ((obj != null) && (obj instanceof Date)) {
            if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
                obj = rs.getTimestamp(index);
            }
        }
        return obj;
    }

    /**
     * Check whether the given SQL type is numeric.
     *
     * @param sqlType the SQL type to be checked
     * @return whether the type is numeric
     */
    public static boolean isNumeric(int sqlType) {
        return (Types.BIT == sqlType) || (Types.BIGINT == sqlType) || (Types.DECIMAL == sqlType) || (Types.DOUBLE ==
            sqlType) || (Types.FLOAT == sqlType) || (Types.INTEGER == sqlType) || (Types.NUMERIC == sqlType) ||
            (Types.REAL == sqlType) || (Types.SMALLINT == sqlType) || (Types.TINYINT == sqlType);
    }

    /**
     * Return a single result object from the given Collection.
     * <p>Throws an exception if 0 or more than 1 element found.
     * @param results the result Collection (can be <code>null</code>)
     * @return the single result object
     */
    public static Object requiredSingleResult(Collection results) throws DatabaseException {
        int size = ((results != null) ? results.size() : 0);
        if (size == 0) {
            throw new DatabaseException("Empty result set, expected one row");
        }
        if (results.size() > 1) {
            throw new DatabaseException("Result set larger than one row");
        }
        return results.iterator().next();
    }

    /**
     * Checks whether a result set has a column matching the specified column name.
     * The column name is first changed to match the database format.
     * E.g. an unquoted columnName in h2 will be converted to uppercase so the column name that is
     * being checked for is really the uppercase version of the column name,
     *
     * @param rs result set to check
     * @param columnNameToCheck column name to check
     * @return The value if found, null if not found
     */
    public static String getValueForColumn(ResultSet rs, String columnNameToCheck, Database database) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        int numberOfColumns = metadata.getColumnCount();
        String correctedColumnName = database.correctObjectName(columnNameToCheck, Column.class);
        // get the column names; column indexes start from 1
        for (int i = 1; i < (numberOfColumns + 1); i++) {
            String columnName = metadata.getColumnLabel(i);
            // Get the name of the column's table name
            if (correctedColumnName.equalsIgnoreCase(columnName)) {
                return rs.getString(columnName);
            }
        }
        return null;
    }


}
