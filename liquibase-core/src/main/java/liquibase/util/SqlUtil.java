package liquibase.util;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.*;
import liquibase.logging.LogFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.core.DataType;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SqlUtil {

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
        String typeName = type.getTypeName();

        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().from(type, database);

        String stringVal = (String) val;
        if (stringVal.isEmpty()) {
            if (liquibaseDataType instanceof CharType) {
                return "";
            } else {
                return null;
            }
        }


        if (database instanceof OracleDatabase && !stringVal.startsWith("'") && !stringVal.endsWith("'")) {
            //oracle returns functions without quotes
            Object maybeDate = null;

            if (liquibaseDataType instanceof DateType || typeId == Types.DATE) {
                if (stringVal.endsWith("'HH24:MI:SS')")) {
                    maybeDate = DataTypeFactory.getInstance().fromDescription("time", database).sqlToObject(stringVal, database);
                } else {
                    maybeDate = DataTypeFactory.getInstance().fromDescription("date", database).sqlToObject(stringVal, database);
                }
            } else if (liquibaseDataType instanceof DateTimeType || typeId == Types.TIMESTAMP) {
                maybeDate = DataTypeFactory.getInstance().fromDescription("datetime", database).sqlToObject(stringVal, database);
            } else if (!stringVal.matches("\\d+\\.?\\d*")) { //not just a number
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

        if (stringVal.startsWith("'") && stringVal.endsWith("'")) {
            stringVal = stringVal.substring(1, stringVal.length() - 1);
        } else if (stringVal.startsWith("((") && stringVal.endsWith("))")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("('") && stringVal.endsWith("')")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("(") && stringVal.endsWith(")")) {
            return new DatabaseFunction(stringVal.substring(1, stringVal.length() - 1));
        }

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
            if (stringVal.startsWith("b'")) { //mysql returns boolean values as b'0' and b'1'
                stringVal = stringVal.replaceFirst("b'", "").replaceFirst("'$", "");
            }
            stringVal = stringVal.trim();
            if (scanner.hasNextBoolean()) {
                return scanner.nextBoolean();
            } else {
                return new Integer(stringVal);
            }
        } else if (liquibaseDataType instanceof BlobType|| typeId == Types.BLOB) {
            return new DatabaseFunction(stringVal);
        } else if ((liquibaseDataType instanceof BooleanType || typeId == Types.BOOLEAN )) {
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
        } else if (liquibaseDataType instanceof NCharType || typeId == Types.NCHAR) {
            return stringVal;
        } else if (typeId == Types.NCLOB) {
            return stringVal;
        } else if (typeId == Types.NULL) {
            return null;
        } else if ((liquibaseDataType instanceof NumberType || typeId == Types.NUMERIC)) {
            if (scanner.hasNextBigDecimal()) {
                return scanner.nextBigDecimal();
            } else {
                return new DatabaseFunction(stringVal);
            }
        } else if (liquibaseDataType instanceof NVarcharType || typeId == Types.NVARCHAR) {
            return stringVal;
        } else if (typeId == Types.OTHER) {
            if (database instanceof DB2Database && typeName.equalsIgnoreCase("DECFLOAT")) {
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
            return DataTypeFactory.getInstance().fromDescription("time", database).sqlToObject(stringVal, database);
        } else if (liquibaseDataType instanceof DateTimeType || liquibaseDataType instanceof TimestampType || typeId == Types.TIMESTAMP) {
            return DataTypeFactory.getInstance().fromDescription("datetime", database).sqlToObject(stringVal, database);
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
        } else {
            LogFactory.getLogger().info("Unknown default value: value '" + stringVal + "' type " + typeName + " (" + type + "), assuming it is a function");
            return new DatabaseFunction(stringVal);
        }
    }
}
