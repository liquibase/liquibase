package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.statement.DatabaseFunction;
import liquibase.util.ISODateFormat;
import liquibase.util.JdbcUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class H2Database extends AbstractJdbcDatabase {

    private static String START_CONCAT = "CONCAT(";
    private static String END_CONCAT = ")";
    private static String SEP_CONCAT = ", ";
    private static List keywords = Arrays.asList(
            "CROSS",
            "CURRENT_DATE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "DISTINCT",
            "EXCEPT",
            "EXISTS",
            "FALSE",
            "FETCH",
            "FOR",
            "FROM",
            "FULL",
            "GROUP",
            "HAVING",
            "INNER",
            "INTERSECT",
            "IS",
            "JOIN",
            "LIKE",
            "LIMIT",
            "MINUS",
            "NATURAL",
            "NOT",
            "NULL",
            "OFFSET",
            "ON",
            "ORDER",
            "PRIMARY",
            "ROWNUM",
            "SELECT",
            "SYSDATE",
            "SYSTIME",
            "SYSTIMESTAMP",
            "TODAY",
            "TRUE",
            "UNION",
            "UNIQUE",
            "WHERE");
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
        boolean isLocalURL = (
                super.isSafeToRunUpdate()
                        || url.startsWith("jdbc:h2:file:")
                        || url.startsWith("jdbc:h2:mem:")
                        || url.startsWith("jdbc:h2:zip:")
                        || url.startsWith("jdbc:h2:~")
        );
        return isLocalURL;
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
        return keywords.contains(objectName.toUpperCase(Locale.US));
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    protected String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
	return "%d";
    }

    @Override
    protected String getAutoIncrementByClause() {
	return "%d";
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
                    LogService.getLog(getClass()).info(LogType.LOG, "Could not read current schema name: "+e.getMessage());
                } finally {
                    JdbcUtils.close(resultSet, statement);
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
            LogFactory.getInstance().getLog().warning("Failed to determine database version, reported error: " + e.getMessage());
        }
        return false;
    }

    private int getBuildVersion() throws DatabaseException {

        Pattern patchVersionPattern = Pattern.compile("^(?:\\d+\\.)(?:\\d+\\.)(\\d+).*$");
        Matcher matcher = patchVersionPattern.matcher(getDatabaseProductVersion());

        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        else {
            LogFactory.getInstance().getLog().warning("Failed to determine H2 build number from product version: " + getDatabaseProductVersion());
            return -1;
        }

    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        // http://www.h2database.com/html/datatypes.html seems to imply 9 digits
        return 9;
    }
}
