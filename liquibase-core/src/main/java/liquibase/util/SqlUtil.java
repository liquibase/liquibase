package liquibase.util;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.*;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.LogService;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtil {

    private SqlUtil() {
        throw new IllegalStateException("This utility class must not be instantiated. Sorry.");
    }

    public static boolean isNumeric(int dataType) {
        List<Integer> numericTypes = Arrays.asList(
                Types.BIGINT,
                Types.BIT,
                Types.INTEGER,
                Types.SMALLINT,
                Types.TINYINT,
                Types.DECIMAL,
                Types.DOUBLE,
                Types.FLOAT,
                Types.NUMERIC,
                Types.REAL
        );

        return numericTypes.contains(dataType);
    }

    public static boolean isBoolean(int dataType) {
        return dataType == Types.BOOLEAN;
    }

    public static boolean isDate(int dataType) {
        List<Integer> validTypes = Arrays.asList(
                Types.DATE,
                Types.TIME,
                Types.TIMESTAMP
        );

        return validTypes.contains(dataType);
    }

    public static Object parseValue(Database database, Object val, DataType type) {
        if (!(val instanceof String)) {
            return val;
        }

        int typeId = Integer.MIN_VALUE;
        if (type.getDataTypeId() != null) {
            typeId = type.getDataTypeId();
        }

        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().from(type, database);

        String stringVal = (String) val;
        if (stringVal.isEmpty()) {
            if (liquibaseDataType instanceof CharType) {
                return "";
            } else {
                return null;
            }
        }

        if ((database instanceof OracleDatabase) && !stringVal.startsWith("'") && !stringVal.endsWith("'")) {
            //oracle returns functions without quotes
            Object maybeDate = null;

            if ((liquibaseDataType instanceof DateType) || (typeId == Types.DATE)) {
                if (stringVal.endsWith("'HH24:MI:SS')")) {
                    maybeDate = DataTypeFactory.getInstance().fromDescription(
                            "time", database).sqlToObject(stringVal, database);
                } else {
                    maybeDate = DataTypeFactory.getInstance().fromDescription(
                            "date", database).sqlToObject(stringVal, database);
                }
            } else if ((liquibaseDataType instanceof DateTimeType) || (typeId == Types.TIMESTAMP)) {
                maybeDate = DataTypeFactory.getInstance().fromDescription(
                        "datetime", database).sqlToObject(stringVal, database);
            } else if (!stringVal.matches("\\d+\\.?\\d*")) {
                //not just a number
                return new DatabaseFunction(stringVal);
            }
            if (maybeDate != null) {
                if (maybeDate instanceof java.util.Date) {
                    return maybeDate;
                } else {
                    return new DatabaseFunction(stringVal);
                }
            }
        }

        boolean strippedSingleQuotes = false;
        if (stringVal.startsWith("'") && stringVal.endsWith("'")) {
            stringVal = stringVal.substring(1, stringVal.length() - 1);
            strippedSingleQuotes = true;
        } else if (stringVal.startsWith("((") && stringVal.endsWith("))")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("('") && stringVal.endsWith("')")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("(") && stringVal.endsWith(")")) {
            return new DatabaseFunction(stringVal.substring(1, stringVal.length() - 1));
        }

        String typeName = type.getTypeName();
        Scanner scanner = new Scanner(stringVal.trim());
        if (typeId == Types.ARRAY) {
            return new DatabaseFunction(stringVal);
        } else if ((liquibaseDataType instanceof BigIntType || typeId == Types.BIGINT)) {
            if (scanner.hasNextBigInteger()) {
                return scanner.nextBigInteger();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (typeId == Types.BINARY) {
            return new DatabaseFunction(stringVal.trim());
        } else if (typeId == Types.BIT) {
            if (stringVal.startsWith("b'") || stringVal.startsWith("B'")) { //mysql returns boolean values as b'0' and b'1'
                stringVal = stringVal.replaceFirst("b'", "").replaceFirst("B'", "").replaceFirst("'$", "");
            }
            //postgres defaults for bit columns look like: B'0'::"bit"
            if (stringVal.endsWith("'::\"bit\"")) {
                stringVal = stringVal.replaceFirst("'::\"bit\"", "");
            }

            stringVal = stringVal.trim();
            if (database instanceof MySQLDatabase) {
                return "1".equals(stringVal) || "true".equalsIgnoreCase(stringVal);
            }

            Object value;
            if (scanner.hasNextBoolean()) {
                value = scanner.nextBoolean();
            } else {
                value = Integer.valueOf(stringVal);
            }

            if (database instanceof MSSQLDatabase && value instanceof Boolean) {
                if ((Boolean) value) {
                    return new DatabaseFunction("'true'");
                } else {
                    return new DatabaseFunction("'false'");
                }
            }
            return value;
        } else if (liquibaseDataType instanceof BlobType || typeId == Types.BLOB) {
            if (strippedSingleQuotes) {
                return stringVal;
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if ((liquibaseDataType instanceof BooleanType || typeId == Types.BOOLEAN)) {
            if (scanner.hasNextBoolean()) {
                return scanner.nextBoolean();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (liquibaseDataType instanceof CharType || typeId == Types.CHAR) {
            return stringVal;
        } else if (liquibaseDataType instanceof ClobType || typeId == Types.CLOB) {
            return stringVal;
        } else if (typeId == Types.DATALINK) {
            return new DatabaseFunction(stringVal);
        } else if (liquibaseDataType instanceof DateType || typeId == Types.DATE) {
            if (typeName.equalsIgnoreCase("year")) {
                return stringVal.trim();
            }
            return DataTypeFactory.getInstance().fromDescription("date", database).sqlToObject(stringVal, database);
        } else if ((liquibaseDataType instanceof DecimalType || typeId == Types.DECIMAL)) {
            if (scanner.hasNextBigDecimal()) {
                return scanner.nextBigDecimal();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (typeId == Types.DISTINCT) {
            return new DatabaseFunction(stringVal);
        } else if ((liquibaseDataType instanceof DoubleType || typeId == Types.DOUBLE)) {
            if (scanner.hasNextDouble()) {
                return scanner.nextDouble();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if ((liquibaseDataType instanceof FloatType || typeId == Types.FLOAT)) {
            if (scanner.hasNextFloat()) {
                return scanner.nextFloat();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if ((liquibaseDataType instanceof IntType || typeId == Types.INTEGER)) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (typeId == Types.JAVA_OBJECT) {
            return new DatabaseFunction(stringVal);
        } else if (typeId == Types.LONGNVARCHAR) {
            return stringVal;
        } else if (typeId == Types.LONGVARBINARY) {
            return new DatabaseFunction(stringVal);
        } else if (typeId == Types.LONGVARCHAR) {
            return stringVal;
        } else if (liquibaseDataType instanceof NCharType || typeId == Types.NCHAR || liquibaseDataType.getName().equalsIgnoreCase("NCLOB")) {
            return stringVal;
        } else if (typeId == Types.NCLOB) {
            return stringVal;
        } else if (typeId == Types.NULL) {
            return null;
        } else if ((liquibaseDataType instanceof NumberType || typeId == Types.NUMERIC)) {
            if (scanner.hasNextBigDecimal()) {
                if (database instanceof MSSQLDatabase && stringVal.endsWith(".0") || stringVal.endsWith
                        (".00") || stringVal.endsWith(".000")) {
                    // MSSQL can store the value with the decimal digits. return it directly to avoid unexpected differences
                    return new DatabaseFunction(stringVal);
                }
                return scanner.nextBigDecimal();
            } else {
                if (stringVal.equals("")) {
                    return new DatabaseFunction("''"); //can have numeric default '' on sql server
                }
                return new DatabaseFunction(stringVal);
            }
        } else if (liquibaseDataType instanceof NVarcharType || typeId == Types.NVARCHAR) {
            return stringVal;
        } else if (typeId == Types.OTHER) {
            if (database instanceof AbstractDb2Database && typeName.equalsIgnoreCase("DECFLOAT")) {
                return new BigDecimal(stringVal);
            }
            return new DatabaseFunction(stringVal);
        } else if (typeId == Types.REAL) {
            return new BigDecimal(stringVal.trim());
        } else if (typeId == Types.REF) {
            return new DatabaseFunction(stringVal);
        } else if (typeId == Types.ROWID) {
            return new DatabaseFunction(stringVal);
        } else if ((liquibaseDataType instanceof SmallIntType || typeId == Types.SMALLINT)) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (typeId == Types.SQLXML) {
            return new DatabaseFunction(stringVal);
        } else if (typeId == Types.STRUCT) {
            return new DatabaseFunction(stringVal);
        } else if (liquibaseDataType instanceof TimeType || typeId == Types.TIME) {
            return DataTypeFactory.getInstance().fromDescription("time", database)
                    .sqlToObject(stringVal, database);
        } else if (liquibaseDataType instanceof DateTimeType || liquibaseDataType instanceof TimestampType ||
                typeId == Types.TIMESTAMP) {
            return DataTypeFactory.getInstance().fromDescription("datetime", database)
                    .sqlToObject(stringVal, database);
        } else if ((liquibaseDataType instanceof TinyIntType || typeId == Types.TINYINT)) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (typeId == Types.VARBINARY) {
            return new DatabaseFunction(stringVal);
        } else if (liquibaseDataType instanceof VarcharType || typeId == Types.VARCHAR) {
            return stringVal;
        } else if (database instanceof MySQLDatabase && typeName.toLowerCase().startsWith("enum")) {
            return stringVal;
        } else if ((database instanceof MSSQLDatabase) && typeName.toLowerCase().startsWith("datetimeoffset")) {
            return stringVal;
        } else {
            if (stringVal.equals("")) {
                return stringVal;
            }
            LogService.getLog(SqlUtil.class).info("Unknown default value: value '" + stringVal +
                    "' type " + typeName + " (" + type + "). Calling it a function so it's not additionally quoted");
            if (strippedSingleQuotes) { //put quotes back
                return new DatabaseFunction("'" + stringVal + "'");
            }
            return new DatabaseFunction(stringVal);

        }
    }

    public static String replacePredicatePlaceholders(Database database, String predicate, List<String> columnNames,
                                                      List<Object> parameters) {
        Matcher matcher = Pattern.compile(":name|\\?|:value").matcher(predicate.trim());
        StringBuffer sb = new StringBuffer();
        Iterator<String> columnNameIter = columnNames.iterator();
        Iterator<Object> paramIter = parameters.iterator();
        while (matcher.find()) {
            if (":name".equals(matcher.group())) {
                while (columnNameIter.hasNext()) {
                    String columnName = columnNameIter.next();
                    if (columnName == null) {
                        continue;
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(
                            database.escapeObjectName(columnName, Column.class))
                    );
                    break;
                }
            } else if (paramIter.hasNext()) {
                Object param = paramIter.next();
                matcher.appendReplacement(sb, Matcher.quoteReplacement(
                        DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database))
                );
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
