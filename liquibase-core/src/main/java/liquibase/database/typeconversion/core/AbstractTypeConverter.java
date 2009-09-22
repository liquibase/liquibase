package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.Database;
import liquibase.database.structure.type.*;
import liquibase.statement.ComputedDateValue;
import liquibase.statement.ComputedNumericValue;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;
import liquibase.exception.DateParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.change.ColumnConfig;

import java.text.ParseException;
import java.sql.Types;
import java.math.BigInteger;

public abstract class AbstractTypeConverter implements TypeConverter {

    public Object convertDatabaseValueToObject(Object value, int databaseDataType, int firstParameter, int secondParameter, Database database) throws ParseException {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return convertToCorrectObjectType(((String) value).replaceFirst("^'", "").replaceFirst("'$", ""), databaseDataType, firstParameter, secondParameter, database);
        } else {
            return value;
        }
    }

    public DataType getDataType(Object object) {
        if (object instanceof BigInteger) {
            return getBigIntType();
        } else if (object instanceof Boolean) {
            return getBooleanType();
        } else if (object instanceof String) {
            return getVarcharType();
        } else if (object instanceof java.sql.Date) {
            return getDateType();
        } else if (object instanceof java.sql.Timestamp) {
            return getDateTimeType();
        } else if (object instanceof java.sql.Time) {
            return getTimeType();
        } else if (object instanceof java.util.Date) {
            return getDateTimeType();
        } else if (object instanceof Double) {
            return getDoubleType();
        } else if (object instanceof Float) {
            return getFloatType();
        } else if (object instanceof Integer) {
            return getIntType();
        } else if (object instanceof Long) {
            return getBigIntType();
        } else {
            throw new UnexpectedLiquibaseException("Unknown object type "+object.getClass().getName());
        }
    }

    protected Object convertToCorrectObjectType(String value, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
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
    public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
        // Parse out data type and precision
        // Example cases: "CLOB", "java.sql.Types.CLOB", "CLOB(10000)", "java.sql.Types.CLOB(10000)
        String dataTypeName = null;
        String precision = null;
        if (columnTypeString.startsWith("java.sql.Types") && columnTypeString.contains("(")) {
            precision = columnTypeString.substring(columnTypeString.indexOf("(") + 1, columnTypeString.indexOf(")"));
            dataTypeName = columnTypeString.substring(columnTypeString.lastIndexOf(".") + 1, columnTypeString.indexOf("("));
        } else if (columnTypeString.startsWith("java.sql.Types")) {
            dataTypeName = columnTypeString.substring(columnTypeString.lastIndexOf(".") + 1);
        } else if (columnTypeString.contains("(")) {
            precision = columnTypeString.substring(columnTypeString.indexOf("(") + 1, columnTypeString.indexOf(")"));
            dataTypeName = columnTypeString.substring(0, columnTypeString.indexOf("("));
        } else {
            dataTypeName = columnTypeString;
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
            if (columnTypeString.startsWith("java.sql.Types")) {
                returnTypeName = getTypeFromMetaData(dataTypeName);
            } else {
                // Don't know what it is, just return it
                return new CustomType(columnTypeString,0,2);
            }
        }

        if (returnTypeName == null) {
            throw new UnexpectedLiquibaseException("Could not determine " + dataTypeName + " for " + this.getClass().getName());
        }

        if (precision != null) {
            String[] params = precision.split(",");
            returnTypeName.setFirstParameter(Integer.parseInt(params[0].trim()));
            if (params.length > 1) {
                returnTypeName.setSecondParameter(Integer.parseInt(params[1].trim()));
            }
        }

         return returnTypeName;
    }

    // Get the type from the Connection MetaData (use the MetaData to translate from java.sql.Types to DB-specific type)
    private DataType getTypeFromMetaData(final String dataTypeName) {
        return null;
//        return new DataType(dataTypeName, false);
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

    public DataType getDataType(ColumnConfig columnConfig) {
        return getDataType(columnConfig.getType(), columnConfig.isAutoIncrement());
    }

    /**
     * Returns the actual database-specific data type to use a "date" (no time information) column.
     */
    public DateType getDateType() {
        return new DateType();
    }

    /**
     * Returns the actual database-specific data type to use a "time" column.
     */
    public TimeType getTimeType() {
        return new TimeType();
    }

    public DateTimeType getDateTimeType() {
        return new DateTimeType();
    }

    public BigIntType getBigIntType() {
        return new BigIntType();
    }

    /**
     * Returns the actual database-specific data type to use for a "char" column.
     */
    public CharType getCharType() {
        return new CharType();
    }

    /**
     * Returns the actual database-specific data type to use for a "varchar" column.
     */
    public VarcharType getVarcharType() {
        return new VarcharType();
    }

    /**
     * Returns the actual database-specific data type to use for a "float" column.
     *
     * @return database-specific type for float
     */
    public FloatType getFloatType() {
        return new FloatType();
    }

    /**
     * Returns the actual database-specific data type to use for a "double" column.
     *
     * @return database-specific type for double
     */
    public DoubleType getDoubleType() {
        return new DoubleType();
    }

    /**
     * Returns the actual database-specific data type to use for a "int" column.
     *
     * @return database-specific type for int
     */
    public IntType getIntType() {
        return new IntType();
    }

    /**
     * Returns the actual database-specific data type to use for a "tinyint" column.
     *
     * @return database-specific type for tinyint
     */
    public TinyIntType getTinyIntType() {
        return new TinyIntType();
    }

    public BooleanType getBooleanType() {
        return new BooleanType();
    }

    public NumberType getNumberType() {
        return new NumberType();
    }

    public CurrencyType getCurrencyType() {
        return new CurrencyType();
    }

    public UUIDType getUUIDType() {
        return new UUIDType();
    }

    public ClobType getClobType() {
        return new ClobType();
    }

    public BlobType getBlobType() {
        return new BlobType();
    }
}
