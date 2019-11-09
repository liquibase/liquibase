package liquibase.executor.jvm;

import liquibase.util.JdbcUtils;
import liquibase.util.NumberUtils;

import java.math.BigDecimal;
import java.sql.*;

/**
 * RowMapper implementation that converts a single column into
 * a single result value per row. Expects to work on a ResultSet
 * that just contains a single column.
 * <p/>
 * <p>The type of the result value for each row can be specified.
 * The value for the single column will be extracted from the ResultSet
 * and converted into the specified target type.
 *
 * @author Spring Framework
 */
class SingleColumnRowMapper implements RowMapper {

    private Class requiredType;


    /**
     * Create a new SingleColumnRowMapper.
     *
     * @see #setRequiredType
     */
    public SingleColumnRowMapper() {
    }

    /**
     * Create a new SingleColumnRowMapper.
     *
     * @param requiredType the type that each result object is expected to match
     */
    public SingleColumnRowMapper(Class requiredType) {
        this.requiredType = requiredType;
    }

    /**
     * Set the type that each result object is expected to match.
     * <p>If not specified, the column value will be exposed as
     * returned by the JDBC driver.
     */
    public void setRequiredType(Class requiredType) {
        this.requiredType = requiredType;
    }


    /**
     * Extract a value for the single column in the current row.
     * <p>Validates that there is only one column selected,
     * then delegates to <code>getColumnValue()</code> and also
     * <code>convertValueToRequiredType</code>, if necessary.
     *
     * @see java.sql.ResultSetMetaData#getColumnCount()
     * @see #getColumnValue(java.sql.ResultSet,int,Class)
     * @see #convertValueToRequiredType(Object,Class)
     */
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Validate column count.
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns == 0) {
            throw new SQLException("Returned no columns!");
        } else if (nrOfColumns != 1) {
            throw new SQLException("Returned too many columns: "+ nrOfColumns);
        }

        // Extract column value from JDBC ResultSet
        Object result = getColumnValue(rs, 1, this.requiredType);
        if ((result != null) && (this.requiredType != null) && !this.requiredType.isInstance(result)) {
            // Extracted value does not match already: try to convert it.
            try {
                return convertValueToRequiredType(result, this.requiredType);
            }
            catch (IllegalArgumentException ex) {
                throw new SQLException(
                        "Type mismatch affecting row number " + rowNum + " and column type '" +
                                rsmd.getColumnTypeName(1) + "': " + ex.getMessage());
            }
        }
        return result;
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation calls <code>ResultSet.getString(index)</code> etc
     * for all standard value types (String, Boolean, number types, date types, etc).
     * It calls <code>ResultSet.getObject(index)</code> else.
     * <p>If no required type has been specified, this method delegates to
     * <code>getColumnValue(rs, index)</code>, which basically calls
     * <code>ResultSet.getObject(index)</code> but applies some additional
     * default conversion to appropriate value types.
     * <p>Explicit extraction of a String is necessary to properly extract an Oracle
     * RAW value as a String, for example. For the other given types, it is also
     * recommendable to extract the desired types explicitly, to let the JDBC driver
     * perform appropriate (potentially database-specific) conversion.
     *
     * @param rs           is the ResultSet holding the data
     * @param index        is the column index
     * @param requiredType the type that each result object is expected to match
     *                     (or <code>null</code> if none specified)
     * @return the Object value
     * @see java.sql.ResultSet#getString(int)
     * @see java.sql.ResultSet#getObject(int)
     * @see #getColumnValue(java.sql.ResultSet,int)
     */
    protected Object getColumnValue(ResultSet rs, int index, Class requiredType) throws SQLException {
        if (requiredType != null) {
            Object value;
            boolean wasNullCheck = false;

            // Explicitly extract typed value, as far as possible.
            if (String.class.equals(requiredType)) {
                value = rs.getString(index);
            } else if (Boolean.class.equals(requiredType)) {
                value = (rs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE);
                wasNullCheck = true;
            } else if (Byte.class.equals(requiredType)) {
                value = rs.getByte(index);
                wasNullCheck = true;
            } else if (Short.class.equals(requiredType)) {
                value = rs.getShort(index);
                wasNullCheck = true;
            } else if (Integer.class.equals(requiredType)) {
                value = rs.getInt(index);
                wasNullCheck = true;
            } else if (Long.class.equals(requiredType)) {
                value = rs.getLong(index);
                wasNullCheck = true;
            } else if (Float.class.equals(requiredType)) {
                value = rs.getFloat(index);
                wasNullCheck = true;
            } else if (Double.class.equals(requiredType) || Number.class.equals(requiredType)) {
                value = rs.getDouble(index);
                wasNullCheck = true;
            } else if (byte[].class.equals(requiredType)) {
                value = rs.getBytes(index);
            } else if (java.sql.Date.class.equals(requiredType)) {
                value = rs.getDate(index);
            } else if (java.sql.Time.class.equals(requiredType)) {
                value = rs.getTime(index);
            } else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
                value = rs.getTimestamp(index);
            } else if (BigDecimal.class.equals(requiredType)) {
                value = rs.getBigDecimal(index);
            } else if (Blob.class.equals(requiredType)) {
                value = rs.getBlob(index);
            } else if (Clob.class.equals(requiredType)) {
                value = rs.getClob(index);
            } else {
                // Some unknown type desired -> rely on getObject.
                value = rs.getObject(index);
            }

            // Perform was-null check if demanded (for results that the
            // JDBC driver returns as primitives).
            if (wasNullCheck && (value != null) && rs.wasNull()) {
                value = null;
            }
            return value;
        } else {
            // No required type specified -> perform default extraction.
            return getColumnValue(rs, index);
        }
    }

    /**
     * Retrieve a JDBC object value for the specified column, using the most
     * appropriate value type. Called if no required type has been specified.
     * <p>The default implementation delegates to <code>JdbcUtils.getResultSetValue()</code>,
     * which uses the <code>ResultSet.getObject(index)</code> method. Additionally,
     * it includes a "hack" to get around Oracle returning a non-standard object for
     * their TIMESTAMP datatype. See the <code>JdbcUtils#getResultSetValue()</code>
     * javadoc for details.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the Object value
     */
    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }

    /**
     * Convert the given column value to the specified required type.
     * Only called if the extracted column value does not match already.
     * <p>If the required type is String, the value will simply get stringified
     * via <code>toString()</code>. In case of a Number, the value will be
     * converted into a Number, either through number conversion or through
     * String parsing (depending on the value type).
     *
     * @param value        the column value as extracted from <code>getColumnValue()</code>
     *                     (never <code>null</code>)
     * @param requiredType the type that each result object is expected to match
     *                     (never <code>null</code>)
     * @return the converted value
     * @see #getColumnValue(java.sql.ResultSet,int,Class)
     */
    protected Object convertValueToRequiredType(Object value, Class requiredType) {
        if (String.class.equals(this.requiredType)) {
            return value.toString();
        } else if (Number.class.isAssignableFrom(this.requiredType)) {
            if (value instanceof Number) {
                // Convert original Number to target Number class.
                return NumberUtils.convertNumberToTargetClass(((Number) value), this.requiredType);
            } else {
                // Convert stringified value to target Number class.
                return NumberUtils.parseNumber(value.toString(), this.requiredType);
            }
        } else {
            throw new IllegalArgumentException(
                    "Value [" + value + "] is of type [" + value.getClass().getName() +
                            "] and cannot be converted to required type [" + this.requiredType.getName() + "]");
        }
    }
}
