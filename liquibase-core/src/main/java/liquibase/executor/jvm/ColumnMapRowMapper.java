package liquibase.executor.jvm;

import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link RowMapper} implementation that creates a <code>java.util.Map</code>
 * for each row, representing all columns as key-value pairs: one
 * entry for each column, with the column name as key.
 * <p/>
 * <p>The Map implementation to use and the key to use for each column
 * in the column Map can be customized through overriding
 * {@link #createColumnMap} and {@link #getColumnKey}, respectively.
 * <p/>
 *
 * @author Spring Framework
 */
@SuppressWarnings({"unchecked"})
public class ColumnMapRowMapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map mapOfColValues = createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String key = getColumnKey(rsmd.getColumnLabel(i));
            Object obj = getColumnValue(rs, i);
            mapOfColValues.put(key, obj);
        }
        return mapOfColValues;
    }

    /**
     * Create a Map instance to be used as column map.
     * @param columnCount the column count, to be used as initial
     *                    capacity for the Map
     * @return the new Map instance
     */
    protected Map createColumnMap(int columnCount) {
        return new LinkedHashMap(columnCount);
    }

    /**
     * Determine the key to use for the given column in the column Map.
     *
     * @param columnName the column name (uppercase) as returned by the ResultSet
     * @return the column key to use
     * @see java.sql.ResultSetMetaData#getColumnName
     */
    protected String getColumnKey(String columnName) {
        return columnName.toUpperCase();
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation uses the <code>getObject</code> method.
     * Additionally, this implementation includes a "hack" to get around Oracle
     * returning a non standard object for their TIMESTAMP datatype.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the Object returned
     */
    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }

}
