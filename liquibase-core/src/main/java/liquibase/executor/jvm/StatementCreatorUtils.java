package liquibase.executor.jvm;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

/**
 * Utility methods for PreparedStatementSetter/Creator and CallableStatementCreator
 * implementations, providing sophisticated parameter management (including support
 * for LOB values).
 * <p/>
 * <p>Used by PreparedStatementCreatorFactory and CallableStatementCreatorFactory,
 * but also available for direct use in custom setter/creator implementations.
 *
 * @author Spring Framework
 * @see PreparedStatementSetter
 * @see SqlParameter
 */
abstract class StatementCreatorUtils {

    /**
     * Set the value for a parameter. The method used is based on the SQL type
     * of the parameter and we can handle complex types like arrays and LOBs.
     *
     * @param ps         the prepared statement or callable statement
     * @param paramIndex index of the parameter we are setting
     * @param param      the parameter as it is declared including type
     * @param inValue    the value to set
     * @throws SQLException if thrown by PreparedStatement methods
     */
    public static void setParameterValue(
            PreparedStatement ps, int paramIndex, SqlParameter param, Object inValue)
            throws SQLException {

        setParameterValueInternal(ps, paramIndex, param.getSqlType(), param.getTypeName(), param.getScale(), inValue);
    }

    /**
     * Set the value for a parameter. The method used is based on the SQL type
     * of the parameter and we can handle complex types like arrays and LOBs.
     *
     * @param ps         the prepared statement or callable statement
     * @param paramIndex index of the parameter we are setting
     * @param sqlType    the SQL type of the parameter
     * @param inValue    the value to set (plain value or a SqlTypeValue)
     * @throws SQLException if thrown by PreparedStatement methods
     */
    public static void setParameterValue(
            PreparedStatement ps, int paramIndex, int sqlType, Object inValue)
            throws SQLException {

        setParameterValueInternal(ps, paramIndex, sqlType, null, null, inValue);
    }

    /**
     * Set the value for a parameter. The method used is based on the SQL type
     * of the parameter and we can handle complex types like arrays and LOBs.
     *
     * @param ps         the prepared statement or callable statement
     * @param paramIndex index of the parameter we are setting
     * @param sqlType    the SQL type of the parameter
     * @param typeName   the type name of the parameter
     *                   (optional, only used for SQL NULL and SqlTypeValue)
     * @param scale      the number of digits after the decimal point
     *                   (for DECIMAL and NUMERIC types)
     * @param inValue    the value to set (plain value or a SqlTypeValue)
     * @throws SQLException if thrown by PreparedStatement methods
     */
    private static void setParameterValueInternal(
            PreparedStatement ps, int paramIndex, int sqlType, String typeName, Integer scale, Object inValue)
            throws SQLException {

        if (inValue == null) {
            if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
                boolean useSetObject = false;
                try {
                    useSetObject = (ps.getConnection().getMetaData().getDatabaseProductName().indexOf("Informix") != -1);
                }
                catch (Throwable ex) {
//                    logger.debug("Could not check database product name", ex);
                }
                if (useSetObject) {
                    ps.setObject(paramIndex, null);
                } else {
                    ps.setNull(paramIndex, Types.NULL);
                }
            } else if (typeName != null) {
                ps.setNull(paramIndex, sqlType, typeName);
            } else {
                ps.setNull(paramIndex, sqlType);
            }
        } else {  // inValue != null
            if (inValue instanceof SqlTypeValue) {
                ((SqlTypeValue) inValue).setTypeValue(ps, paramIndex, sqlType, typeName);
            } else if ((sqlType == Types.VARCHAR) || (sqlType == -9)) { //-9 is Types.NVARCHAR in java 1.6
                ps.setString(paramIndex, inValue.toString());
            } else if ((sqlType == Types.DECIMAL) || (sqlType == Types.NUMERIC)) {
                if (inValue instanceof BigDecimal) {
                    ps.setBigDecimal(paramIndex, (BigDecimal) inValue);
                } else if (scale != null) {
                    ps.setObject(paramIndex, inValue, sqlType, scale);
                } else {
                    ps.setObject(paramIndex, inValue, sqlType);
                }
            } else if (sqlType == Types.DATE) {
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Date) {
                        ps.setDate(paramIndex, (java.sql.Date) inValue);
                    } else {
                        ps.setDate(paramIndex, new java.sql.Date(((java.util.Date) inValue).getTime()));
                    }
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.DATE);
                }
            } else if (sqlType == Types.TIME) {
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Time) {
                        ps.setTime(paramIndex, (java.sql.Time) inValue);
                    } else {
                        ps.setTime(paramIndex, new java.sql.Time(((java.util.Date) inValue).getTime()));
                    }
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.TIME);
                }
            } else if (sqlType == Types.TIMESTAMP) {
                if (inValue instanceof java.util.Date) {
                    if (inValue instanceof java.sql.Timestamp) {
                        ps.setTimestamp(paramIndex, (java.sql.Timestamp) inValue);
                    } else {
                        ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
                    }
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
                } else {
                    ps.setObject(paramIndex, inValue, Types.TIMESTAMP);
                }
            } else if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
                if (isStringValue(inValue)) {
                    ps.setString(paramIndex, inValue.toString());
                } else if (isDateValue(inValue)) {
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
                } else if (inValue instanceof Calendar) {
                    Calendar cal = (Calendar) inValue;
                    ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()));
                } else {
                    // Fall back to generic setObject call without SQL type specified.
                    ps.setObject(paramIndex, inValue);
                }
            } else {
                // Fall back to generic setObject call with SQL type specified.
                ps.setObject(paramIndex, inValue, sqlType);
            }
        }
    }

    /**
     * Check whether the given value can be treated as a String value.
     */
    private static boolean isStringValue(Object inValue) {
        return ((inValue instanceof CharSequence) || (inValue instanceof StringWriter));
    }

    /**
     * Check whether the given value is a <code>java.util.Date</code>
     * (but not one of the JDBC-specific subclasses).
     */
    private static boolean isDateValue(Object inValue) {
        return ((inValue instanceof java.util.Date) && !((inValue instanceof java.sql.Date) || (inValue instanceof
            java.sql.Time) || (inValue instanceof java.sql.Timestamp)));
    }

}
