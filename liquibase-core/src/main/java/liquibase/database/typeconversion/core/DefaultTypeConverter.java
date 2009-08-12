package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.DateParseException;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.ComputedNumericValue;
import liquibase.change.ColumnConfig;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;

import java.text.ParseException;
import java.sql.Types;
import java.math.BigInteger;

public class DefaultTypeConverter implements TypeConverter {

    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);
    private static final DataType TIME_TYPE = new DataType("TIME", false);
    private static final DataType BIGINT_TYPE = new DataType("BIGINT", true);
    private static final DataType NUMBER_TYPE = new DataType("NUMBER", true);
    private static final DataType CHAR_TYPE = new DataType("CHAR", true);
    private static final DataType VARCHAR_TYPE = new DataType("VARCHAR", true);
    private static final DataType FLOAT_TYPE = new DataType("FLOAT", true);
    private static final DataType DOUBLE_TYPE = new DataType("DOUBLE", true);
    private static final DataType INT_TYPE = new DataType("INT", true);
    private static final DataType TINYINT_TYPE = new DataType("TINYINT", true);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("CLOB", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);

    public int getDatabaseType(int type) {
        int returnType = type;
        if (returnType == java.sql.Types.BOOLEAN) {
            String booleanType = getBooleanType().getDataTypeName();
            if (!booleanType.equalsIgnoreCase("boolean")) {
                returnType = java.sql.Types.TINYINT;
            }
        }

        return returnType;
    }
    
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue == null) {
            return null;
        } else if (defaultValue instanceof String) {
            return convertToCorrectJavaType(((String) defaultValue).replaceFirst("^'", "").replaceFirst("'$", ""), dataType, columnSize, decimalDigits, database);
        } else {
            return defaultValue;
        }
    }

    public String convertJavaObjectToString(Object value, Database database) {
        if (value != null) {
            if (value instanceof String) {
                if ("null".equalsIgnoreCase(((String) value))) {
                    return null;
                }
                return "'" + ((String) value).replaceAll("'", "''") + "'";
            } else if (value instanceof Number) {
                return value.toString();
            } else if (value instanceof Boolean) {
                String returnValue;
                if (((Boolean) value)) {
                    returnValue = this.getTrueBooleanValue();
                } else {
                    returnValue = this.getFalseBooleanValue();
                }
                if (returnValue.matches("\\d+")) {
                    return returnValue;
                } else {
                    return "'" + returnValue + "'";
                }
            } else if (value instanceof java.sql.Date) {
                return database.getDateLiteral(((java.sql.Date) value));
            } else if (value instanceof java.sql.Time) {
                return database.getDateLiteral(((java.sql.Time) value));
            } else if (value instanceof java.sql.Timestamp) {
                return database.getDateLiteral(((java.sql.Timestamp) value));
            } else if (value instanceof ComputedDateValue) {
                return ((ComputedDateValue) value).getValue();
            } else {
                throw new RuntimeException("Unknown default value type: " + value.getClass().getName());
            }
        } else {
            return null;
        }
    }
    

        protected Object convertToCorrectJavaType(String value, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (value == null) {
            return null;
        }
        if (dataType == Types.CLOB || dataType == Types.VARCHAR || dataType == Types.CHAR || dataType == Types.LONGVARCHAR) {
            if (value.equalsIgnoreCase("NULL")) {
                return null;
            } else {
                return value;
            }
        }

        value = StringUtils.trimToNull(value);
        if (value == null) {
            return null;
        }

        try {
            if (dataType == Types.DATE) {
                return new java.sql.Date(database.parseDate(value).getTime());
            } else if (dataType == Types.TIMESTAMP) {
                return new java.sql.Timestamp(database.parseDate(value).getTime());
            } else if (dataType == Types.TIME) {
                return new java.sql.Time(database.parseDate(value).getTime());
            } else if (dataType == Types.BIGINT) {
                return new BigInteger(value);
            } else if (dataType == Types.BIT) {
                value = value.replaceFirst("b'", ""); //mysql puts wierd chars in bit field
                if (value.equalsIgnoreCase("true")) {
                    return Boolean.TRUE;
                } else if (value.equalsIgnoreCase("false")) {
                    return Boolean.FALSE;
                } else if (value.equals("1")) {
                    return Boolean.TRUE;
                } else if (value.equals("0")) {
                    return Boolean.FALSE;
                } else if (value.equals("(1)")) {
                    return Boolean.TRUE;
                } else if (value.equals("(0)")) {
                    return Boolean.FALSE;
                }
                throw new ParseException("Unknown bit value: " + value, 0);
            } else if (dataType == Types.BOOLEAN) {
                return Boolean.valueOf(value);
            } else if (dataType == Types.DECIMAL) {
                if (decimalDigits == 0) {
                    return new Integer(value);
                }
                return new Double(value);
            } else if (dataType == Types.DOUBLE || dataType == Types.NUMERIC) {
                return new Double(value);
            } else if (dataType == Types.FLOAT) {
                return new Float(value);
            } else if (dataType == Types.INTEGER) {
                return new Integer(value);
            } else if (dataType == Types.NULL) {
                return null;
            } else if (dataType == Types.REAL) {
                return new Float(value);
            } else if (dataType == Types.SMALLINT) {
                return new Integer(value);
            } else if (dataType == Types.TINYINT) {
                return new Integer(value);
            } else if (dataType == Types.BLOB) {
                return "!!!!!! LIQUIBASE CANNOT OUTPUT BLOB VALUES !!!!!!";
            } else {
                LogFactory.getLogger().warning("Do not know how to convert type " + dataType);
                return value;
            }
        } catch (DateParseException e) {
            return new ComputedDateValue(value);
        } catch (NumberFormatException e) {
            return new ComputedNumericValue(value);
        }
    }

      /**
     * Returns the database-specific datatype for the given column configuration.
     * This method will convert some generic column types (e.g. boolean, currency) to the correct type
     * for the current database.
     */
    public String getColumnType(String columnType, Boolean autoIncrement) {
        // Parse out data type and precision
        // Example cases: "CLOB", "java.sql.Types.CLOB", "CLOB(10000)", "java.sql.Types.CLOB(10000)
        String dataTypeName = null;
        String precision = null;
        if (columnType.startsWith("java.sql.Types") && columnType.contains("(")) {
            precision = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            dataTypeName = columnType.substring(columnType.lastIndexOf(".") + 1, columnType.indexOf("("));
        } else if (columnType.startsWith("java.sql.Types")) {
            dataTypeName = columnType.substring(columnType.lastIndexOf(".") + 1);
        } else if (columnType.contains("(")) {
            precision = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            dataTypeName = columnType.substring(0, columnType.indexOf("("));
        } else {
            dataTypeName = columnType;
        }

        // Translate type to database-specific type, if possible
        DataType returnTypeName = null;
        if (dataTypeName.equalsIgnoreCase("BIGINT")) {
            returnTypeName = getBigIntType();
        } else if (dataTypeName.equalsIgnoreCase("NUMBER")) {
            returnTypeName = getNumberType();
        } else if (dataTypeName.equalsIgnoreCase("BLOB")) {
            returnTypeName = getBlobType();
        } else if (dataTypeName.equalsIgnoreCase("BOOLEAN")) {
            returnTypeName = getBooleanType();
        } else if (dataTypeName.equalsIgnoreCase("CHAR")) {
            returnTypeName = getCharType();
        } else if (dataTypeName.equalsIgnoreCase("CLOB")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("CURRENCY")) {
            returnTypeName = getCurrencyType();
        } else if (dataTypeName.equalsIgnoreCase("DATE")) {
            returnTypeName = getDateType();
        } else if (dataTypeName.equalsIgnoreCase("DATETIME")) {
            returnTypeName = getDateTimeType();
        } else if (dataTypeName.equalsIgnoreCase("DOUBLE")) {
            returnTypeName = getDoubleType();
        } else if (dataTypeName.equalsIgnoreCase("FLOAT")) {
            returnTypeName = getFloatType();
        } else if (dataTypeName.equalsIgnoreCase("INT")) {
            returnTypeName = getIntType();
        } else if (dataTypeName.equalsIgnoreCase("INTEGER")) {
            returnTypeName = getIntType();
        } else if (dataTypeName.equalsIgnoreCase("LONGVARBINARY")) {
            returnTypeName = getBlobType();
        } else if (dataTypeName.equalsIgnoreCase("LONGVARCHAR")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("TEXT")) {
            returnTypeName = getClobType();
        } else if (dataTypeName.equalsIgnoreCase("TIME")) {
            returnTypeName = getTimeType();
        } else if (dataTypeName.equalsIgnoreCase("TIMESTAMP")) {
            returnTypeName = getDateTimeType();
        } else if (dataTypeName.equalsIgnoreCase("TINYINT")) {
            returnTypeName = getTinyIntType();
        } else if (dataTypeName.equalsIgnoreCase("UUID")) {
            returnTypeName = getUUIDType();
        } else if (dataTypeName.equalsIgnoreCase("VARCHAR")) {
            returnTypeName = getVarcharType();
        } else {
            if (columnType.startsWith("java.sql.Types")) {
                returnTypeName = getTypeFromMetaData(dataTypeName);
            } else {
                // Don't know what it is, just return it
                return columnType;
            }
        }

        if (returnTypeName == null) {
            throw new UnexpectedLiquibaseException("Could not determine " + dataTypeName + " for " + this.getClass().getName());
        }

        // Return type and precision, if any
        if (precision != null && returnTypeName.getSupportsPrecision()) {
            return returnTypeName.getDataTypeName() + "(" + precision + ")";
        } else {
            return returnTypeName.getDataTypeName();
        }
    }

        // Get the type from the Connection MetaData (use the MetaData to translate from java.sql.Types to DB-specific type)
    private DataType getTypeFromMetaData(final String dataTypeName) {
        return new DataType(dataTypeName, false);
//todo: reintroduce        ResultSet resultSet = null;
//        try {
//            Integer requestedType = (Integer) Class.forName("java.sql.Types").getDeclaredField(dataTypeName).get(null);
//            DatabaseConnection connection = getConnection();
//            if (connection == null) {
//                throw new RuntimeException("Cannot evaluate java.sql.Types without a connection");
//            }
//            resultSet = connection.getMetaData().getTypeInfo();
//            while (resultSet.next()) {
//                String typeName = resultSet.getString("TYPE_NAME");
//                int dataType = resultSet.getInt("DATA_TYPE");
//                int maxPrecision = resultSet.getInt("PRECISION");
//                if (requestedType == dataType) {
//                    if (maxPrecision > 0) {
//                        return new DataType(typeName, true);
//                    } else {
//                        return new DataType(typeName, false);
//                    }
//                }
//            }
//            // Connection MetaData does not contain the type, return null
//            return null;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (resultSet != null) {
//                try {
//                    resultSet.close();
//                } catch (DatabaseException e) {
//                    // Can't close result set, no handling required
//                }
//            }
//        }
    }

    public String getColumnType(ColumnConfig columnConfig) {
        return getColumnType(columnConfig.getType(), columnConfig.isAutoIncrement());
    }

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue() {
        return "0";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue() {
        return "1";
    }

        /**
     * Returns the actual database-specific data type to use a "date" (no time information) column.
     */
    public DataType getDateType() {
        return DATE_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use a "time" column.
     */
    public DataType getTimeType() {
        return TIME_TYPE;
    }

    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    public DataType getBigIntType() {
        return BIGINT_TYPE;
    }

    public DataType getNumberType() {
        return NUMBER_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "char" column.
     */
    public DataType getCharType() {
        return CHAR_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "varchar" column.
     */
    public DataType getVarcharType() {
        return VARCHAR_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "float" column.
     *
     * @return database-specific type for float
     */
    public DataType getFloatType() {
        return FLOAT_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "double" column.
     *
     * @return database-specific type for double
     */
    public DataType getDoubleType() {
        return DOUBLE_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "int" column.
     *
     * @return database-specific type for int
     */
    public DataType getIntType() {
        return INT_TYPE;
    }

    /**
     * Returns the actual database-specific data type to use for a "tinyint" column.
     *
     * @return database-specific type for tinyint
     */
    public DataType getTinyIntType() {
        return TINYINT_TYPE;
    }

    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    public DataType getClobType() {
        return CLOB_TYPE;
    }

    public DataType getBlobType() {
        return BLOB_TYPE;
    }

}
