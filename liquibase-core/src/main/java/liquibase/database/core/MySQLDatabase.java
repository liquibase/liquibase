package liquibase.database.core;

import java.math.BigInteger;
import java.util.*;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    private static Set<String> reservedWords = new HashSet();

    public MySQLDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
        // objects in mysql are always case sensitive
        super.quotingStartCharacter ="`";
        super.quotingEndCharacter="`";
    }

    @Override
    public String getShortName() {
        return "mysql";
    }


//todo: handle    @Override
//    public String getConnectionUsername() throws DatabaseException {
//        return super.getConnection().getConnectionUserName().replaceAll("\\@.*", "");
//    }

    @Override
    public String correctObjectName(String name, Class<? extends DatabaseObject> objectType) {
        if (objectType.equals(PrimaryKey.class) && name.equals("PRIMARY")) {
            return null;
        } else {
            name = super.correctObjectName(name, objectType);
            if (name == null) {
                return null;
            }
            if (!this.isCaseSensitive()) {
                return name.toLowerCase();
            }
            return name;
        }
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "MySQL";
    }

    @Override
    public Integer getDefaultPort() {
        return 3306;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
    }


    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return super.mustQuoteObjectName(objectName, objectType) || (!objectName.contains("(") && !objectName.matches("\\w+"));
    }

    @Override
    public String getLineComment() {
        return "-- ";
    }

    @Override
    protected String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    @Override
    protected boolean generateAutoIncrementStartWith(final BigInteger startWith) {
    	// startWith not supported here. StartWith has to be set as table option.
        return false;
    }

    public String getTableOptionAutoIncrementStartWithClause(BigInteger startWith){
    	String startWithClause = String.format(getAutoIncrementStartWithClause(), (startWith == null) ? defaultAutoIncrementStartWith : startWith);
    	return getAutoIncrementClause() + startWithClause;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        // incrementBy not supported
        return false;
    }

    @Override
    protected String getAutoIncrementOpening() {
        return "";
    }

    @Override
    protected String getAutoIncrementClosing() {
        return "";
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
        return "=%d";
    }

    @Override
    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        returnString.append("CONCAT_WS(");
        for (String value : values) {
            returnString.append(value).append(", ");
        }

        return returnString.toString().replaceFirst(", $", ")");
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return escapeObjectName(indexName, Index.class);
    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return true;
    }

    @Override
    public boolean disableForeignKeyChecks() throws DatabaseException {
        boolean enabled = ExecutorService.getInstance().getExecutor(this).queryForInt(new RawSqlStatement("SELECT @@FOREIGN_KEY_CHECKS")) == 1;
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=0"));
        return enabled;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=1"));
    }

    @Override
    public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
        return new CatalogAndSchema(rawCatalogName, null).customize(this);
    }

    @Override
    public String escapeStringForDatabase(String string) {
        string = super.escapeStringForDatabase(string);
        if (string == null) {
            return null;
        }
        return string.replace("\\", "\\\\");
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return true;
    }

    @Override
    public boolean isReservedWord(String string) {
        if (reservedWords.contains(string.toUpperCase())) {
            return true;
        }
        return super.isReservedWord(string);
    }

    public int getDatabasePatchVersion() throws DatabaseException {
        String versionStrings[] = this.getDatabaseProductVersion().split("\\.");
        try {
            return Integer.parseInt(versionStrings[2].replaceFirst("\\D.*", ""));
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    {
        //list from http://dev.mysql.com/doc/refman/5.6/en/reserved-words.html
        reservedWords.addAll(Arrays.asList("ACCESSIBLE",
                "ADD",
                "ALL",
                "ALTER",
                "ANALYZE",
                "AND",
                "AS",
                "ASC",
                "ASENSITIVE",
                "BEFORE",
                "BETWEEN",
                "BIGINT",
                "BINARY",
                "BLOB",
                "BOTH",
                "BY",
                "CALL",
                "CASCADE",
                "CASE",
                "CHANGE",
                "CHAR",
                "CHARACTER",
                "CHECK",
                "COLLATE",
                "COLUMN",
                "CONDITION",
                "CONSTRAINT",
                "CONTINUE",
                "CONVERT",
                "CREATE",
                "CROSS",
                "CURRENT_DATE",
                "CURRENT_TIME",
                "CURRENT_TIMESTAMP",
                "CURRENT_USER",
                "CURSOR",
                "DATABASE",
                "DATABASES",
                "DAY_HOUR",
                "DAY_MICROSECOND",
                "DAY_MINUTE",
                "DAY_SECOND",
                "DEC",
                "DECIMAL",
                "DECLARE",
                "DEFAULT",
                "DELAYED",
                "DELETE",
                "DESC",
                "DESCRIBE",
                "DETERMINISTIC",
                "DISTINCT",
                "DISTINCTROW",
                "DIV",
                "DOUBLE",
                "DROP",
                "DUAL",
                "EACH",
                "ELSE",
                "ELSEIF",
                "ENCLOSED",
                "ESCAPED",
                "EXISTS",
                "EXIT",
                "EXPLAIN",
                "FALSE",
                "FETCH",
                "FLOAT",
                "FLOAT4",
                "FLOAT8",
                "FOR",
                "FORCE",
                "FOREIGN",
                "FROM",
                "FULLTEXT",
                "GET",
                "GRANT",
                "GROUP",
                "HAVING",
                "HIGH_PRIORITY",
                "HOUR_MICROSECOND",
                "HOUR_MINUTE",
                "HOUR_SECOND",
                "IF",
                "IGNORE",
                "IN",
                "INDEX",
                "INFILE",
                "INNER",
                "INOUT",
                "INSENSITIVE",
                "INSERT",
                "INT",
                "INT1",
                "INT2",
                "INT3",
                "INT4",
                "INT8",
                "INTEGER",
                "INTERVAL",
                "INTO",
                "IS",
                "ITERATE",
                "JOIN",
                "KEY",
                "KEYS",
                "KILL",
                "LEADING",
                "LEAVE",
                "LEFT",
                "LIKE",
                "LIMIT",
                "LINEAR",
                "LINES",
                "LOAD",
                "LOCALTIME",
                "LOCALTIMESTAMP",
                "LOCK",
                "LONG",
                "LONGBLOB",
                "LONGTEXT",
                "LOOP",
                "LOW_PRIORITY",
                "MASTER_SSL_VERIFY_SERVER_CERT",
                "MATCH",
                "MAXVALUE",
                "MEDIUMBLOB",
                "MEDIUMINT",
                "MEDIUMTEXT",
                "MIDDLEINT",
                "MINUTE_MICROSECOND",
                "MINUTE_SECOND",
                "MOD",
                "MODIFIES",
                "NATURAL",
                "NOT",
                "NO_WRITE_TO_BINLOG",
                "NULL",
                "NUMERIC",
                "ON",
                "OPTIMIZE",
                "OPTION",
                "OPTIONALLY",
                "OR",
                "ORDER",
                "OUT",
                "OUTER",
                "OUTFILE",
                "PARTITION",
                "PRECISION",
                "PRIMARY",
                "PROCEDURE",
                "PURGE",
                "RANGE",
                "READ",
                "READS",
                "READ_WRITE",
                "REAL",
                "REFERENCES",
                "REGEXP",
                "RELEASE",
                "RENAME",
                "REPEAT",
                "REPLACE",
                "REQUIRE",
                "RESIGNAL",
                "RESTRICT",
                "RETURN",
                "REVOKE",
                "RIGHT",
                "RLIKE",
                "SCHEMA",
                "SCHEMAS",
                "SECOND_MICROSECOND",
                "SELECT",
                "SENSITIVE",
                "SEPARATOR",
                "SET",
                "SHOW",
                "SIGNAL",
                "SMALLINT",
                "SPATIAL",
                "SPECIFIC",
                "SQL",
                "SQLEXCEPTION",
                "SQLSTATE",
                "SQLWARNING",
                "SQL_BIG_RESULT",
                "SQL_CALC_FOUND_ROWS",
                "SQL_SMALL_RESULT",
                "SSL",
                "STARTING",
                "STRAIGHT_JOIN",
                "TABLE",
                "TERMINATED",
                "THEN",
                "TINYBLOB",
                "TINYINT",
                "TINYTEXT",
                "TO",
                "TRAILING",
                "TRIGGER",
                "TRUE",
                "UNDO",
                "UNION",
                "UNIQUE",
                "UNLOCK",
                "UNSIGNED",
                "UPDATE",
                "USAGE",
                "USE",
                "USING",
                "UTC_DATE",
                "UTC_TIME",
                "UTC_TIMESTAMP",
                "VALUES",
                "VARBINARY",
                "VARCHAR",
                "VARCHARACTER",
                "VARYING",
                "WHEN",
                "WHERE",
                "WHILE",
                "WITH",
                "WRITE",
                "XOR",
                "YEAR_MONTH",
                "ZEROFILL"));
    }}
