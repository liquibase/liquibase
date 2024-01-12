package liquibase.util;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.*;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.US;

public abstract class SqlUtil {

    private static final String NAME_REGEX = ":name|\\?|:value";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    public static boolean isNumeric(int dataType) {
        switch (dataType) {
            case Types.BIGINT:
            case Types.BIT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
            case Types.REAL:
                return true;
            default:
                return false;
        }
    }

    public static boolean isBoolean(int dataType) {
        return dataType == Types.BOOLEAN;
    }

    public static boolean isDate(int dataType) {
        switch (dataType) {
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return true;
            default:
                return false;
        }
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

        if ((database instanceof PostgresDatabase || database instanceof OracleDatabase) &&
            (liquibaseDataType instanceof CharType || liquibaseDataType instanceof ClobType) &&
            stringVal.toUpperCase(ENGLISH).startsWith("GENERATED ALWAYS AS ")
        ) {
            return new DatabaseFunction(stringVal);
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
        try (Scanner scanner = new Scanner(stringVal.trim()).useLocale(US)) {
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

                Object value = stringVal;
                if (scanner.hasNextBoolean()) {
                    value = scanner.nextBoolean();
                } else if (scanner.hasNextInt()) {
                    if (stringVal.length() > 1) {
                        stringVal = stringVal.substring(0, 1);
                    }
                    value = Integer.valueOf(stringVal);
                }

                // Make sure we handle BooleanType values which are not Boolean
                if (database instanceof MSSQLDatabase) {
                    if (value instanceof Boolean) {
                        if ((Boolean) value) {
                            return new DatabaseFunction("'true'");
                        } else {
                            return new DatabaseFunction("'false'");
                        }
                    } else if (value instanceof Integer) {
                        if (((Integer) value) != 0) {
                            return new DatabaseFunction("'true'");
                        } else {
                            return new DatabaseFunction("'false'");
                        }
                    } else {
                        // you can declare in MsSQL: `col_name bit default 'nonsense'`
                        return new DatabaseFunction(String.format("'%s'", value));
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
                return strippedSingleQuotes ? stringVal : new DatabaseFunction(stringVal);
            } else if (typeId == Types.LONGVARBINARY) {
                return new DatabaseFunction(stringVal);
            } else if (typeId == Types.LONGVARCHAR) {
                return strippedSingleQuotes ? stringVal : new DatabaseFunction(stringVal);
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
                Scope.getCurrentScope().getLog(SqlUtil.class).info("Unknown default value: value '" + stringVal +
                        "' type " + typeName + " (" + type + "). Calling it a function so it's not additionally quoted");
                if (strippedSingleQuotes) { //put quotes back
                    return new DatabaseFunction("'" + stringVal + "'");
                }
                return new DatabaseFunction(stringVal);

            }
        }
    }

    public static String replacePredicatePlaceholders(Database database, String predicate, List<String> columnNames,
                                                      List<Object> parameters) {
        Matcher matcher = NAME_PATTERN.matcher(predicate.trim());
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

    /**
     * Get the string sql statements from a given SqlStatement
     * @param statement the statement to stringify
     * @param sqlGeneratorFactory the SqlGeneratorFactory instance to use to generate the sql
     * @param database the database to generate sql against
     * @return the sql string or an empty string if there are no statements to generate
     */
    public static String getSqlString(SqlStatement statement, SqlGeneratorFactory sqlGeneratorFactory, Database database) {
        Sql[] sqlStatements = sqlGeneratorFactory.generateSql(statement, database);
        return convertSqlArrayToString(sqlStatements);
    }

    /**
     * Given an array of sql, get the string sql statements.
     * @param sqlStatements the statements to stringify
     * @return the sql string or an empty string if there are no statements to generate
     */
    public static String convertSqlArrayToString(Sql[] sqlStatements) {
        if (sqlStatements != null) {
            return Arrays.stream(sqlStatements)
                    .map(sql -> sql.toSql().endsWith(sql.getEndDelimiter()) ? sql.toSql() : sql.toSql() + sql.getEndDelimiter())
                    .collect(Collectors.joining("\n"));
        } else {
            return "";
        }
    }
}
