package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.structure.DatabaseObject;
import liquibase.util.ISODateFormat;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HsqlDatabase extends AbstractJdbcDatabase {
    private static String START_CONCAT = "CONCAT(";
    private static String END_CONCAT = ")";
    private static String SEP_CONCAT = ", ";

    private Boolean oracleSyntax;

    public HsqlDatabase() {
    	super.unquotedObjectsAreUppercased=true;
        super.setCurrentDateTimeFunction("NOW");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
    	super.defaultAutoIncrementStartWith = BigInteger.ZERO;
        super.sequenceCurrentValueFunction = "CURRVAL('%s')";
    }
    
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "HSQL Database Engine".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:hsqldb:")) {
            return "org.hsqldb.jdbcDriver";
        }
        return null;
    }


    @Override
    public Integer getDefaultPort() {
        return 9001;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "HyperSQL";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public String getShortName() {
        return "hsqldb";
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        try {
            if (getDatabaseMajorVersion() < 2) {
                return false;
            } else {
                return true;
            }
        } catch (DatabaseException e) {
            return true;
        }
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        if (supportsCatalogs()) {
            return "PUBLIC";
        } else {
            return null;
        }

    }

    @Override
    protected String getConnectionSchemaName() {
        return "PUBLIC";
    }

    @Override
    public String getConcatSql(String... values) {
        if (values == null) {
            return null;
        }

        return getConcatSql(Arrays.asList(values));
    }

    /**
     * Recursive way of building CONCAT instruction
     *
     * @param values a non null List of String
     * @return a String containing the CONCAT instruction with all elements, or only a value if there is only one element in the list
     */
    private String getConcatSql(List<String> values) {
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return START_CONCAT + values.get(0) + SEP_CONCAT + getConcatSql(values.subList(1, values.size())) + END_CONCAT;
        }
    }

    @Override
    public String getDateLiteral(String isoDate) {
        String returnString = isoDate;
        try {
            if (isDateTime(isoDate)) {
                ISODateFormat isoTimestampFormat = new ISODateFormat();
                DateFormat dbTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                returnString = dbTimestampFormat.format(isoTimestampFormat.parse(isoDate));
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unexpected date format: " + isoDate, e);
        }
        return "'" + returnString + "'";
    }

    @Override
    public Date parseDate(String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(" ") > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateAsString);
            } else {
                if (dateAsString.indexOf(":") > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean isReservedWord(String value) {
        return keywords.contains(value.toUpperCase());
    }

    private static List keywords = Arrays.asList(
            "ADD",
            "ALL",
            "ALLOCATE",
            "ALTER",
            "AND",
            "ANY",
            "ARE",
            "ARRAY",
            "AS",
            "ASENSITIVE",
            "ASYMMETRIC",
            "AT",
            "ATOMIC",
            "AUTHORIZATION",
            "AVG",
            "BEGIN",
            "BETWEEN",
            "BIGINT",
            "BINARY",
            "BLOB",
            "BOOLEAN",
            "BOTH",
            "BY",
            "CALL",
            "CALLED",
            "CASCADED",
            "CASE",
            "CAST",
            "CHAR",
            "CHARACTER",
            "CHECK",
            "CLOB",
            "CLOSE",
            "COLLATE",
            "COLUMN",
            "COMMIT",
            "CONDITION",
            "CONNECT",
            "CONSTRAINT",
            "CONTINUE",
            "CORRESPONDING",
            "COUNT",
            "CREATE",
            "CROSS",
            "CUBE",
            "CURRENT",
            "CURRENT_DATE",
            "CURRENT_DEFAULT_TRANSFORM_GRO",
            "CURRENT_PATH",
            "CURRENT_ROLE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_TRANSFORM_GROUP_FOR_T",
            "CURRENT_USER",
            "CURSOR",
            "DATE",
            "DAY",
            "DEALLOCATE",
            "DEC",
            "DECIMAL",
            "DECLARE",
            "DEFAULT",
            "DELETE",
            "DEREF",
            "DESCRIBE",
            "DETERMINISTIC",
            "DISCONNECT",
            "DISTINCT",
            "DO",
            "DOUBLE",
            "DROP",
            "DYNAMIC",
            "EACH",
            "ELEMENT",
            "ELSE",
            "ELSEIF",
            "END",
            "ESCAPE",
            "EXCEPT",
            "EXEC",
            "EXECUTE",
            "EXISTS",
            "EXIT",
            "EXTERNAL",
            "FALSE",
            "FETCH",
            "FILTER",
            "FLOAT",
            "FOR",
            "FOREIGN",
            "FREE",
            "FROM",
            "FULL",
            "FUNCTION",
            "GET",
            "GLOBAL",
            "GRANT",
            "GROUP",
            "HAVING",
            "HOLD",
            "HOUR",
            "IDENTITY",
            "IF",
            "IMMEDIATE",
            "IN",
            "INDICATOR",
            "INNER",
            "INOUT",
            "INPUT",
            "INSENSITIVE",
            "INSERT",
            "INT",
            "INTEGER",
            "INTERSECT",
            "INTERVAL",
            "INTO",
            "IS",
            "ITERATE",
            "JOIN",
            "LANGUAGE",
            "LARGE",
            "LEADING",
            "LEAVE",
            "LEFT",
            "LIKE",
            "LOCAL",
            "LOCALTIME",
            "LOCALTIMESTAMP",
            "LOOP",
            "MATCH",
            "MAX",
            "MEMBER",
            "MERGE",
            "METHOD",
            "MIN",
            "MINUTE",
            "MODIFIES",
            "MODULE",
            "MONTH",
            "MULTISET",
            "NATIONAL",
            "NATURAL",
            "NCHAR",
            "NCLOB",
            "NEW",
            "NO",
            "NONE",
            "NOT",
            "NULL",
            "NUMERIC",
            "OF",
            "ON",
            "ONLY",
            "OPEN",
            "OR",
            "ORDER",
            "OUT",
            "OUTER",
            "OUTPUT",
            "OVER",
            "OVERLAPS",
            "PARAMETER",
            "PARTITION",
            "PRECISION",
            "PREPARE",
            "PRIMARY",
            "PROCEDURE",
            "RANGE",
            "READS",
            "REAL",
            "RECURSIVE",
            "REF",
            "REFERENCES",
            "REFERENCING",
            "RELEASE",
            "REPEAT",
            "RESIGNAL",
            "RESULT",
            "RETURN",
            "RETURNS",
            "REVOKE",
            "RIGHT",
            "ROLLBACK",
            "ROLLUP",
            "ROW",
            "ROWS",
            "SAVEPOINT",
            "SCOPE",
            "SCROLL",
            "SEARCH",
            "SELECT",
            "SENSITIVE",
            "SESSION_USER",
            "SET",
            "SIGNAL",
            "SIMILAR",
            "SMALLINT",
            "SOME",
            "SPECIFIC",
            "SPECIFICTYPE",
            "SQL",
            "SQLEXCEPTION",
            "SQLSTATE",
            "SQLWARNING",
            "START",
            "STATIC",
            "SUBMULTISET",
            "SYMMETRIC",
            "SYSTEM",
            "SYSTEM_USER",
            "TABLE",
            "TABLESAMPLE",
            "THEN",
            "TIME",
            "TIMESTAMP",
            "TIMEZONE_HOUR",
            "TIMEZONE_MINUTE",
            "TO",
            "TRAILING",
            "TREAT",
            "TRIGGER",
            "TRUE",
            "UNDO",
            "UNION",
            "UNIQUE",
            "UNNEST",
            "UNTIL",
            "UPDATE",
            "USER",
            "USING",
            "VALUE",
            "VALUES",
            "VARCHAR",
            "VARYING",
            "WHEN",
            "WHENEVER",
            "WHERE",
            "WHILE",
            "WINDOW",
            "WITHIN",
            "WITHOUT",
            "YEAR",
            "ALIAS",
            "AUTOCOMMIT",
            "CACHED",
            "CHECKPOINT",
            "EXPLAIN",
            "IGNORECASE",
            "INDEX",
            "LOGSIZE",
            "MATCHED",
            "MAXROWS",
            "MEMORY",
            "MINUS",
            "NEXT",
            "OPENBRACKET",
            "PLAN",
            "PROPERTY",
            "READONLY",
            "REFERENTIAL_INTEGRITY",
            "RENAME",
            "RESTART",
            "SCRIPT",
            "SCRIPTFORMAT",
            "SEMICOLON",
            "SHUTDOWN",
            "TEMP",
            "TEXT",
            "VIEW",
            "WRITE_DELAY",
            "VAR_POP",
            "VAR_SAMP",
            "STDDEV_POP",
            "STDDEV_SAMP",
            "DEFRAG",
            "INCREMENT",
            "TOCHAR",
            "DATABASE",
            "SCHEMA",
            "ROLE",
            "DOW",
            "INITIAL");

    @Override
    public boolean isCaseSensitive() {
        return false;
    }
    
    @Override
    public void setConnection(DatabaseConnection conn) {
        oracleSyntax = null;
        super.setConnection(conn);
    }

    public boolean isUsingOracleSyntax() {
        if (oracleSyntax == null) {
            oracleSyntax = Boolean.FALSE;
            if (getConnection() != null && getConnection().getURL() != null) {
                for (String str : getConnection().getURL().split(";")) {
                    if (str.contains("sql.syntax_ora") && str.contains("=")) {
                        oracleSyntax = Boolean.valueOf(str.split("=")[1].trim());
                        break;
                    }
                }
            }
        }
        return oracleSyntax;
    }


    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
            return super.escapeObjectName(objectName, objectType);
        }
        if (objectName != null && quotingStrategy != ObjectQuotingStrategy.QUOTE_ALL_OBJECTS && isReservedWord(objectName.toUpperCase())) {
                return "\""+objectName.toUpperCase()+"\"";
        }
        return objectName;
    }
}
