package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.CatalogAndSchema.CatalogAndSchemaCase;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtil;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static liquibase.util.BooleanUtil.isTrue;

public class H2Database extends AbstractJdbcDatabase {

    private static final String PATCH_VERSION_REGEX = "^(?:\\d+\\.)(?:\\d+\\.)(\\d+).*$";
    private static final Pattern PATCH_VERSION_PATTERN = Pattern.compile(PATCH_VERSION_REGEX);
    private static final String START_CONCAT = "CONCAT(";
    private static final String END_CONCAT = ")";
    private static final String SEP_CONCAT = ", ";
    private static final String LEGAL_IDENTIFIER_REGEX = "^[a-zA-Z_][a-zA-Z_0-9]*$";
    private static final Pattern LEGAL_IDENTIFIER_PATTERN = Pattern.compile(LEGAL_IDENTIFIER_REGEX);
    protected static final Set<String> V2_RESERVED_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "ALL",
            "AND",
            "ANY",
            "ARRAY",
            "AS",
            "ASYMMETRIC",
            "AUTHORIZATION",
            "BETWEEN",
            "BOTH",
            "CASE",
            "CAST",
            "CHECK",
            "CONSTRAINT",
            "CROSS",
            "CURRENT_CATALOG",
            "CURRENT_DATE",
            "CURRENT_PATH",
            "CURRENT_ROLE",
            "CURRENT_SCHEMA",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_USER",
            "DAY",
            "DEFAULT",
            "DISTINCT",
            "ELSE",
            "END",
            "EXCEPT",
            "EXISTS",
            "FALSE",
            "FETCH",
            "FILTER",
            "FOR",
            "FOREIGN",
            "FROM",
            "FULL",
            "GROUP",
            "GROUPS",
            "HAVING",
            "HOUR",
            "IF",
            "ILIKE",
            "IN",
            "INNER",
            "INTERSECT",
            "INTERSECTS",
            "INTERVAL",
            "IS",
            "JOIN",
            "KEY",
            "LEADING",
            "LEFT",
            "LIKE",
            "LIMIT",
            "LOCALTIME",
            "LOCALTIMESTAMP",
            "MINUS",
            "MINUTE",
            "MONTH",
            "NATURAL",
            "NOT",
            "NULL",
            "OFFSET",
            "ON",
            "OR",
            "ORDER",
            "OVER",
            "PARTITION",
            "PRIMARY",
            "QUALIFY",
            "RANGE",
            "REGEXP",
            "RIGHT",
            "ROW",
            "ROWNUM",
            "ROWS",
            "SELECT",
            "SESSION_USER",
            "SET",
            "SOME",
            "SYMMETRIC",
            "SYSDATE",
            "SYSTEM_USER",
            "SYSTIME",
            "SYSTIMESTAMP",
            "TABLE",
            "TO",
            "TODAY",
            "TOP",
            "TRAILING",
            "TRUE",
            "UNESCAPE",
            "UNION",
            "UNIQUE",
            "UNKNOWN",
            "USER",
            "USING",
            "VALUE",
            "VALUES",
            "WHEN",
            "WHERE",
            "WINDOW",
            "WITH",
            "YEAR",
            "_ROWID_"
    )));
    protected static final Set<String> V1_RESERVED_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            "ALL",
            "AND",
            "ARRAY",
            "AS",
            "BETWEEN",
            "BOTH",
            "CASE",
            "CHECK",
            "CONSTRAINT",
            "CROSS",
            "CURRENT_CATALOG",
            "CURRENT_DATE",
            "CURRENT_SCHEMA",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_USER",
            "DISTINCT",
            "EXCEPT",
            "EXISTS",
            "FALSE",
            "FETCH",
            "FILTER",
            "FOR",
            "FOREIGN",
            "FROM",
            "FULL",
            "GROUP",
            "GROUPS",
            "HAVING",
            "IF",
            "ILIKE",
            "IN",
            "INNER",
            "INTERSECT",
            "INTERSECTS",
            "INTERVAL",
            "IS",
            "JOIN",
            "LEADING",
            "LEFT",
            "LIKE",
            "LIMIT",
            "LOCALTIME",
            "LOCALTIMESTAMP",
            "MINUS",
            "NATURAL",
            "NOT",
            "NULL",
            "OFFSET",
            "ON",
            "OR",
            "ORDER",
            "OVER",
            "PARTITION",
            "PRIMARY",
            "QUALIFY",
            "RANGE",
            "REGEXP",
            "RIGHT",
            "ROW",
            "ROWNUM",
            "ROWS",
            "SELECT",
            "SYSDATE",
            "SYSTIME",
            "SYSTIMESTAMP",
            "TABLE",
            "TODAY",
            "TOP",
            "TRAILING",
            "TRUE",
            "UNION",
            "UNIQUE",
            "UNKNOWN",
            "USING",
            "VALUES",
            "WHERE",
            "WINDOW",
            "WITH",
            "_ROWID_"
    )));
    private String connectionSchemaName = "PUBLIC";

    private static final int MAJOR_VERSION_FOR_MINMAX_IN_SEQUENCES = 1;
    private static final int MINOR_VERSION_FOR_MINMAX_IN_SEQUENCES = 3;
    private static final int BUILD_VERSION_FOR_MINMAX_IN_SEQUENCES = 175;

    public H2Database() {
        super.unquotedObjectsAreUppercased=true;
        super.setCurrentDateTimeFunction("NOW()");
        // for current date
        this.dateFunctions.add(new DatabaseFunction("CURRENT_DATE"));
        this.dateFunctions.add(new DatabaseFunction("CURDATE"));
        this.dateFunctions.add(new DatabaseFunction("SYSDATE"));
        this.dateFunctions.add(new DatabaseFunction("TODAY"));
        // for current time
        this.dateFunctions.add(new DatabaseFunction("CURRENT_TIME"));
        this.dateFunctions.add(new DatabaseFunction("CURTIME"));
        // for current timestamp
        this.dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
        this.dateFunctions.add(new DatabaseFunction("NOW"));

        super.sequenceNextValueFunction = "NEXTVAL('%s')";
        super.sequenceCurrentValueFunction = "CURRVAL('%s')";
        // According to http://www.h2database.com/html/datatypes.html, retrieved on 2017-06-05
        super.unmodifiableDataTypes.addAll(Arrays.asList("int", "integer", "mediumint", "int4", "signed", "boolean",
                "bit", "bool", "tinyint", "smallint", "int2", "year", "bigint", "int8", "identity", "float", "float8",
                "real", "float4", "time", "date", "timestamp", "datetime", "smalldatetime", "timestamp with time zone",
                "other", "uuid", "array", "geometry"));
    }

    @Override
    public String getShortName() {
        return "h2";
    }

    @Override
    public Integer getDefaultPort() {
        return 8082;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "H2";
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:h2")) {
            return "org.h2.Driver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "H2".equals(conn.getDatabaseProductName());
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
        String definition = super.getViewDefinition(schema, name);
        if (!definition.startsWith("SELECT")) {
            definition = definition.replaceFirst(".*?\\n", ""); //some h2 versions return "create view....as\nselect
        }

        definition = definition.replaceFirst("/\\*.*",""); //sometimes includes comments at the end
        return definition;
    }

    @Override
    public Date parseDate(String dateAsString) throws DateParseException {
        try {
            return new ISODateFormat().parse(dateAsString);
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
        if (getConnection() == null) {
            return true;
        }
        String url = getConnection().getURL();
        return (
                super.isSafeToRunUpdate()
                        || (!url.startsWith("jdbc:h2:tcp:") && (!url.startsWith("jdbc:h2:ssl:"))) // exclude remote URLs
        );
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    protected String getConnectionSchemaName() {
        return connectionSchemaName;
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
        return "'" + isoDate.replace('T', ' ') + "'";
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return super.isReservedWord(objectName) || getReservedWords().contains(objectName.toUpperCase(Locale.US));
    }

    protected Set<String> getReservedWords() {
        try {
            if (getDatabaseMajorVersion() >= 2) {
                return V2_RESERVED_WORDS;
            }
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(getClass())
                    .warning("Failed to determine database version, reported error: " + e.getMessage());
        }
        return V1_RESERVED_WORDS;
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        if (isCatalogOrSchemaType(objectType)) {
            if (isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
                    || getSchemaAndCatalogCase() == CatalogAndSchemaCase.ORIGINAL_CASE) {
                return objectName;
            }
            if (getSchemaAndCatalogCase() == CatalogAndSchemaCase.UPPER_CASE) {
                return objectName.toUpperCase(Locale.US);
            }
            if (getSchemaAndCatalogCase() == CatalogAndSchemaCase.LOWER_CASE) {
                return objectName.toLowerCase(Locale.US);
            }
        }
        if (unquotedObjectsAreUppercased == null || quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
            return objectName;
        }
        if (isTrue(unquotedObjectsAreUppercased)) {
            return objectName.toUpperCase(Locale.US);
        }
        return objectName.toLowerCase(Locale.US);
    }

    @Override
    public String escapeObjectName(String objectName, final Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return mustQuoteObjectName(objectName, objectType)
                ? quoteObject(correctObjectName(objectName, objectType), objectType) : objectName;
    }

    @Override
    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
                || isCatalogOrSchemaType(objectType)
                        && (isTrue(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())
                                || getSchemaAndCatalogCase() == CatalogAndSchemaCase.ORIGINAL_CASE)
                || isIllegalIdentifier(objectName) || isReservedWord(objectName);
    }

    private boolean isIllegalIdentifier(String objectName) {
        return !LEGAL_IDENTIFIER_PATTERN.matcher(objectName).matches();
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    protected String getAutoIncrementClause() {
        try {
            if (getDatabaseMajorVersion() == 1) {
                return "AUTO_INCREMENT";
            } else {
                return "GENERATED BY DEFAULT AS IDENTITY";
            }
        } catch (DatabaseException e) {
            return "AUTO_INCREMENT";
        }
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
        try {
            if (getDatabaseMajorVersion() == 1) {
                return "%d";
            } else {
                return super.getAutoIncrementStartWithClause();
            }
        } catch (DatabaseException e) {
            return "%d";
        }
    }

    @Override
    protected String getAutoIncrementByClause() {
        try {
            if (getDatabaseMajorVersion() == 1) {
                return "%d";
            } else {
                return super.getAutoIncrementByClause();
            }
        } catch (DatabaseException e) {
            return "%d";
        }
    }

    @Override
    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy, String generationType, Boolean defaultOnNull) {
        final String clause = super.getAutoIncrementClause(startWith, incrementBy, generationType, defaultOnNull);
        if (clause.startsWith("AUTO_INCREMENT")) {
            return clause;
        }

        return clause.replace(",", ""); //h2 doesn't use commas between the values
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return true;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return true;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        Connection sqlConn = null;

        if (!(conn instanceof OfflineConnection)) {
            try {
                if (conn instanceof JdbcConnection) {
                    Method wrappedConn = conn.getClass().getMethod("getWrappedConnection");
                    wrappedConn.setAccessible(true);
                    sqlConn = (Connection) wrappedConn.invoke(conn);
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }

            if (sqlConn != null) {
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = sqlConn.createStatement();
                    resultSet = statement.executeQuery("SELECT SCHEMA()");
                    String schemaName = null;
                    if (resultSet.next()) {
                        schemaName = resultSet.getString(1);
                    }
                    if (schemaName != null) {
                        this.connectionSchemaName = schemaName;
                    }
                } catch (SQLException e) {
                    Scope.getCurrentScope().getLog(getClass()).info("Could not read current schema name: "+e.getMessage());
                } finally {
                    JdbcUtil.close(resultSet, statement);
                }
            }
        }
        super.setConnection(conn);
    }

    public boolean supportsMinMaxForSequences() {

        try {
            if (getDatabaseMajorVersion() > MAJOR_VERSION_FOR_MINMAX_IN_SEQUENCES) {

                return true;
            } else if (getDatabaseMajorVersion() == MAJOR_VERSION_FOR_MINMAX_IN_SEQUENCES
                       && getDatabaseMinorVersion() > MINOR_VERSION_FOR_MINMAX_IN_SEQUENCES) {

                return true;
            } else if (getDatabaseMajorVersion() == MAJOR_VERSION_FOR_MINMAX_IN_SEQUENCES
                       && getDatabaseMinorVersion() == MINOR_VERSION_FOR_MINMAX_IN_SEQUENCES
                       && getBuildVersion() >= BUILD_VERSION_FOR_MINMAX_IN_SEQUENCES) {
                return true;
            }

        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to determine database version, reported error: " + e.getMessage());
        }
        return false;
    }

    private int getBuildVersion() throws DatabaseException {

        Matcher matcher = PATCH_VERSION_PATTERN.matcher(getDatabaseProductVersion());

        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        else {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to determine H2 build number from product version: " + getDatabaseProductVersion());
            return -1;
        }

    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        // http://www.h2database.com/html/datatypes.html seems to imply 9 digits
        return 9;
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }
}
