package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutorService;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractJdbcDatabase {
    private static final String PRODUCT_NAME = "MySQL";
    private static final Set<String> RESERVED_WORDS = createReservedWords();

    /** Pattern used to extract function precision like 3 in CURRENT_TIMESTAMP(3) */
    private static final String  PRECISION_REGEX = "\\(\\d+\\)";
    public static final Pattern PRECISION_PATTERN = Pattern.compile(PRECISION_REGEX);

    public MySQLDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
    }

    @Override
    public String getShortName() {
        return "mysql";
    }

    @Override
    public String correctObjectName(String name, Class<? extends DatabaseObject> objectType) {
        if (objectType.equals(PrimaryKey.class) && "PRIMARY".equals(name)) {
            return null;
        } else {
            name = super.correctObjectName(name, objectType);
            if (name == null) {
                return null;
            }
            if (!this.isCaseSensitive()) {
                return name.toLowerCase(Locale.US);
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
        // If it looks like a MySQL, swims like a MySQL and quacks like a MySQL,
        // it may still not be a MySQL, but a MariaDB.
        return (
                (PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName()))
                        && (!conn.getDatabaseProductVersion().toLowerCase().contains("mariadb") &&
                            !conn.getDatabaseProductVersion().toLowerCase().contains("clustrix"))
        );
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url != null && url.toLowerCase().startsWith("jdbc:mysql")) {
            String cjDriverClassName = "com.mysql.cj.jdbc.Driver";
            try {

                //make sure we don't have an old jdbc driver that doesn't have this class
                Class.forName(cjDriverClassName);
                return cjDriverClassName;
            } catch (ClassNotFoundException e) {
                //
                // Try to load the class again with the current thread classloader
                //
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                try {
                    Class.forName(cjDriverClassName, true, cl);
                    return cjDriverClassName;
                } catch (ClassNotFoundException cnfe) {
                    return "com.mysql.jdbc.Driver";
                }
            }

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

    public String getTableOptionAutoIncrementStartWithClause(BigInteger startWith) {
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
        StringBuilder returnString = new StringBuilder();
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
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Schema.class.isAssignableFrom(object)) {
            return false;
        }
        if (Sequence.class.isAssignableFrom(object)) {
            return false;
        }
        return super.supports(object);
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
        boolean enabled = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForInt(new RawParameterizedSqlStatement("SELECT @@FOREIGN_KEY_CHECKS")) == 1;
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).execute(new RawParameterizedSqlStatement("SET FOREIGN_KEY_CHECKS=0"));
        return enabled;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).execute(new RawParameterizedSqlStatement("SET FOREIGN_KEY_CHECKS=1"));
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
        if (RESERVED_WORDS.contains(string.toUpperCase())) {
            return true;
        }
        return super.isReservedWord(string);
    }

    public int getDatabasePatchVersion() throws DatabaseException {
        String databaseProductVersion = this.getDatabaseProductVersion();
        if (databaseProductVersion == null) {
            return 0;
        }

        String versionStrings[] = databaseProductVersion.split("\\.");
        try {
            return Integer.parseInt(versionStrings[2].replaceFirst("\\D.*", ""));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            return 0;
        }

    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {

        int major = 0;
        int minor = 0;
        int patch = 0;

        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
            patch = getDatabasePatchVersion();
        } catch (DatabaseException x) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    "Unable to determine exact database server version"
                            + " - specified TIMESTAMP precision"
                            + " will not be set: ", x);
            return 0;
        }

        String minimumVersion = getMinimumVersionForFractionalDigitsForTimestamp();

        if (StringUtil.isMinimumVersion(minimumVersion, major, minor, patch))
            return 6;
        else
            return 0;
    }

    /**
     * Checks whether this instance of a MySQL database is equal to or greater than the specified version.
     *
     * @param minimumVersion the minimum version to check
     * @return {@code true} if this instance of a MySQL database is equal to or greater than the specified version,  {@code false} otherwise
     */
    public boolean isMinimumMySQLVersion(String minimumVersion) {
        int major = 0;
        int minor = 0;
        int patch = 0;
        try {
            major = getDatabaseMajorVersion();
            minor = getDatabaseMinorVersion();
            patch = getDatabasePatchVersion();
        } catch (DatabaseException x) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    "Unable to determine exact database server version");
            return false;
        }
        return StringUtil.isMinimumVersion(minimumVersion, major, minor, patch);
    }

    protected String getMinimumVersionForFractionalDigitsForTimestamp() {
        // MySQL 5.6.4 introduced fractional support...
        // https://dev.mysql.com/doc/refman/5.7/en/data-types.html
        return "5.6.4";
    }

    @Override
    protected String getQuotingStartCharacter() {
        return "`"; // objects in mysql are always case sensitive

    }

    @Override
    protected String getQuotingEndCharacter() {
        return "`"; // objects in mysql are always case sensitive
    }

    /**
     * <p>Returns the default timestamp fractional digits if nothing is specified.</p>
     * <a href="https://dev.mysql.com/doc/refman/5.7/en/fractional-seconds.html">fractional seconds</a>:
     * "The fsp value, if given, must be in the range 0 to 6. A value of 0 signifies that there is no fractional part.
     * If omitted, the default precision is 0. (This differs from the STANDARD SQL default of 6, for compatibility
     * with previous MySQL versions.)"
     *
     * @return always 0
     */
    @Override
    public int getDefaultFractionalDigitsForTimestamp() {
        return 0;
    }

    /*
     * list from http://dev.mysql.com/doc/refman/5.6/en/reserved-words.html
     */
    private static Set<String> createReservedWords() {
        return new HashSet<>(Arrays.asList("ACCESSIBLE",
                "ADD",
                "ADMIN",
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
                "BUCKETS",
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
                "CLONE",
                "COMPONENT",
                "CUBE",
                "CUME_DIST",
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
                "DEFINITION",
                "DELAYED",
                "DELETE",
                "DENSE_RANK",
                "DESC",
                "DESCRIBE",
                "DESCRIPTION",
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
                "EMPTY",
                "ENCLOSED",
                "ESCAPED",
                "EXCEPT",
                "EXCLUDE",
                "EXISTS",
                "EXIT",
                "EXPLAIN",
                "FALSE",
                "FETCH",
                "FIRST_VALUE",
                "FLOAT",
                "FLOAT4",
                "FLOAT8",
                "FOLLOWING",
                "FOR",
                "FORCE",
                "FOREIGN",
                "FROM",
                "FULLTEXT",
                "GEOMCOLLECTION",
                "GENERATED",
                "GET",
                "GET_MASTER_PUBLIC_KEY",
                "GRANT",
                "GROUP",
                "GROUPING",
                "GROUPS",
                "HAVING",
                "HIGH_PRIORITY",
                "HISTOGRAM",
                "HISTORY",
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
                "INVISIBLE",
                "JOIN",
                "JSON_TABLE",
                "KEY",
                "KEYS",
                "KILL",
                "LAG",
                "LAST_VALUE",
                "LEAD",
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
                "LOCKED",
                "LONG",
                "LONGBLOB",
                "LONGTEXT",
                "LOOP",
                "LOW_PRIORITY",
                "MASTER_PUBLIC_KEY_PATH",
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
                "NESTED",
                "NOT",
                "NOWAIT",
                "NO_WRITE_TO_BINLOG",
                "NTH_VALUE",
                "NTILE",
                "NULL",
                "NULLS",
                "NUMERIC",
                "OF",
                "ON",
                "OPTIMIZE",
                "OPTIMIZER_COSTS",
                "OPTION",
                "OPTIONALLY",
                "OR",
                "ORDER",
                "ORDINALITY",
                "ORGANIZATION",
                "OUT",
                "OUTER",
                "OUTFILE",
                "OTHERS",
                "OVER",
                "PARTITION",
                "PATH",
                "PERCENT_RANK",
                "PERSIST",
                "PERSIST_ONLY",
                "PRECEDING",
                "PRECISION",
                "PRIMARY",
                "PROCEDURE",
                "PROCESS",
                "PURGE",
                "RANGE",
                "RANK",
                "READ",
                "READS",
                "READ_WRITE",
                "REAL",
                "RECURSIVE",
                "REFERENCE",
                "REFERENCES",
                "REGEXP",
                "RELEASE",
                "REMOTE",
                "RENAME",
                "REPEAT",
                "REPLACE",
                "REQUIRE",
                "RESIGNAL",
                "RESOURCE",
                "RESPECT",
                "RESTART",
                "RESTRICT",
                "RETURN",
                "REUSE",
                "REVOKE",
                "RIGHT",
                "RLIKE",
                "ROLE",
                "ROW_NUMBER",
                "SCHEMA",
                "SCHEMAS",
                "SECOND_MICROSECOND",
                "SELECT",
                "SENSITIVE",
                "SEPARATOR",
                "SET",
                "SHOW",
                "SIGNAL",
                "SKIP",
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
                "SRID",
                "SSL",
                "STARTING",
                "STORED",
                "STRAIGHT_JOIN",
                "SYSTEM",
                "TABLE",
                "TERMINATED",
                "THEN",
                "THREAD_PRIORITY",
                "TIES",
                "TINYBLOB",
                "TINYINT",
                "TINYTEXT",
                "TO",
                "TRAILING",
                "TRIGGER",
                "TRUE",
                "UNBOUNDED",
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
                "VCPU",
                "VISIBLE",
                "VIRTUAL",
                "WHEN",
                "WHERE",
                "WHILE",
                "WINDOW",
                "WITH",
                "WRITE",
                "XOR",
                "YEAR_MONTH",
                "ZEROFILL"
        ));
    }

    protected String getCurrentDateTimeFunction(int precision) {
        return currentDateTimeFunction.replace("()", "("+precision+")");
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        if (databaseFunction.getValue() != null && isCurrentTimeFunction(databaseFunction.getValue().toLowerCase())) {
            if (databaseFunction.getValue().toLowerCase().contains("on update")) {
                return databaseFunction.getValue();
            }
            int precision = extractPrecision(databaseFunction);
            return precision != 0 ? getCurrentDateTimeFunction(precision) : getCurrentDateTimeFunction();
        }
        return super.generateDatabaseFunctionValue(databaseFunction);
    }

    private int extractPrecision(DatabaseFunction databaseFunction) {
        int precision = 0;
        Matcher precisionMatcher = PRECISION_PATTERN.matcher(databaseFunction.getValue());
        if (precisionMatcher.find()) {
            precision = Integer.parseInt(precisionMatcher.group().replaceAll("[(,)]", ""));
        }
        return precision;
    }

    public void warnAboutAlterColumn(String changeName, Warnings warnings ) {
        warnings.addWarning("Due to " + this.getShortName() + " SQL limitations, " + changeName + " will lose primary key/autoincrement/not null/comment settings explicitly redefined in the change. Use <sql> or <modifySql> to re-specify all configuration if this is the case");
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }

    public boolean getUseAffectedRows() throws DatabaseException {
        return getConnection().getURL().contains("useAffectedRows=true");
    }

}
