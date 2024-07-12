package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutorService;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.RawCallStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.ResourceBundle.getBundle;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractJdbcDatabase {

    private static final String PROXY_USER_REGEX = ".*(?:thin|oci)\\:(.+)/@.*";
	public static final Pattern PROXY_USER_PATTERN = Pattern.compile(PROXY_USER_REGEX);

    private static final String VERSION_REGEX = "(\\d+)\\.(\\d+)\\..*";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

    public static final String PRODUCT_NAME = "oracle";
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    protected final int SHORT_IDENTIFIERS_LENGTH = 30;
    protected final int LONG_IDENTIFIERS_LEGNTH = 128;
    public static final int ORACLE_12C_MAJOR_VERSION = 12;
    public static final int ORACLE_23C_MAJOR_VERSION = 23;

    private final Set<String> reservedWords = new HashSet<>();
    private Set<String> userDefinedTypes;
    private Map<String, String> savedSessionNlsSettings;

    private Boolean canAccessDbaRecycleBin;
    private Integer databaseMajorVersion;
    private Integer databaseMinorVersion;

    /**
     * Default constructor for an object that represents the Oracle Database DBMS.
     */
    public OracleDatabase() {
        super.unquotedObjectsAreUppercased = true;
        //noinspection HardCodedStringLiteral
        super.setCurrentDateTimeFunction("SYSTIMESTAMP");
        // Setting list of Oracle's native functions
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSDATE"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
        //noinspection HardCodedStringLiteral
        dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
        //noinspection HardCodedStringLiteral
        super.sequenceNextValueFunction = "%s.nextval";
        //noinspection HardCodedStringLiteral
        super.sequenceCurrentValueFunction = "%s.currval";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    private void tryProxySession(final String url, final Connection con) {
        Matcher m = PROXY_USER_PATTERN.matcher(url);
        if (m.matches()) {
            Properties props = new Properties();
            props.put("PROXY_USER_NAME", m.group(1));
            try {
                Method method = con.getClass().getMethod("openProxySession", int.class, Properties.class);
                method.setAccessible(true);
                method.invoke(con, 1, props);
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).info("Could not open proxy session on OracleDatabase: " + e.getCause().getMessage());
                return;
            }
            try {
                Method method = con.getClass().getMethod("isProxySession");
                method.setAccessible(true);
                boolean b = (boolean)method.invoke(con);
                if (! b) {
                    Scope.getCurrentScope().getLog(getClass()).info("Proxy session not established on OracleDatabase: ");
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).info("Could not open proxy session on OracleDatabase: " + e.getCause().getMessage());
            }
        }
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,
        // HardCodedStringLiteral
        reservedWords.addAll(Arrays.asList("GROUP", "USER", "SESSION", "PASSWORD", "RESOURCE", "START", "SIZE", "UID", "DESC", "ORDER")); //more reserved words not returned by driver

        Connection sqlConn = null;
        if (!(conn instanceof OfflineConnection)) {
            try {
                /*
                 * Don't try to call getWrappedConnection if the conn instance is
                 * is not a JdbcConnection. This happens for OfflineConnection.
                 * see https://liquibase.jira.com/browse/CORE-2192
                 */
                if (conn instanceof JdbcConnection) {
                    sqlConn = ((JdbcConnection) conn).getWrappedConnection();
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }

            if (sqlConn != null) {
                tryProxySession(conn.getURL(), sqlConn);

                try {
                    //noinspection HardCodedStringLiteral
                    reservedWords.addAll(Arrays.asList(sqlConn.getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
                } catch (SQLException e) {
                    //noinspection HardCodedStringLiteral
                    Scope.getCurrentScope().getLog(getClass()).info("Could get sql keywords on OracleDatabase: " + e.getMessage());
                    //can not get keywords. Continue on
                }
                try {
                    Method method = sqlConn.getClass().getMethod("setRemarksReporting", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(sqlConn, true);
                } catch (Exception e) {
                    //noinspection HardCodedStringLiteral
                    Scope.getCurrentScope().getLog(getClass()).info("Could not set remarks reporting on OracleDatabase: " + e.getMessage());

                    //cannot set it. That is OK
                }

                CallableStatement statement = null;
                try {
                    //noinspection HardCodedStringLiteral
                    statement = sqlConn.prepareCall("{call DBMS_UTILITY.DB_VERSION(?,?)}");
                    statement.registerOutParameter(1, Types.VARCHAR);
                    statement.registerOutParameter(2, Types.VARCHAR);
                    statement.execute();

                    String compatibleVersion = statement.getString(2);
                    if (compatibleVersion != null) {
                        Matcher majorVersionMatcher = VERSION_PATTERN.matcher(compatibleVersion);
                        if (majorVersionMatcher.matches()) {
                            this.databaseMajorVersion = Integer.valueOf(majorVersionMatcher.group(1));
                            this.databaseMinorVersion = Integer.valueOf(majorVersionMatcher.group(2));
                        }
                    }
                } catch (SQLException e) {
                    @SuppressWarnings("HardCodedStringLiteral") String message = "Cannot read from DBMS_UTILITY.DB_VERSION: " + e.getMessage();

                    //noinspection HardCodedStringLiteral
                    Scope.getCurrentScope().getLog(getClass()).info("Could not set check compatibility mode on OracleDatabase, assuming not running in any sort of compatibility mode: " + message);
                } finally {
                    JdbcUtil.closeStatement(statement);
                }

                if (GlobalConfiguration.DDL_LOCK_TIMEOUT.getCurrentValue() != null) {
                    int timeoutValue = GlobalConfiguration.DDL_LOCK_TIMEOUT.getCurrentValue();
                    Scope.getCurrentScope().getLog(getClass()).fine("Setting DDL_LOCK_TIMEOUT value to " + timeoutValue);
                    String sql = "ALTER SESSION SET DDL_LOCK_TIMEOUT=" + timeoutValue;
                    PreparedStatement ddlLockTimeoutStatement = null;
                    try {
                        ddlLockTimeoutStatement = sqlConn.prepareStatement(sql);
                        ddlLockTimeoutStatement.execute();
                    } catch (SQLException sqle) {
                        Scope.getCurrentScope().getUI().sendErrorMessage("Unable to set the DDL_LOCK_TIMEOUT_VALUE: " + sqle.getMessage(), sqle);
                        Scope.getCurrentScope().getLog(getClass()).warning("Unable to set the DDL_LOCK_TIMEOUT_VALUE: " + sqle.getMessage(), sqle);
                    } finally {
                        JdbcUtil.closeStatement(ddlLockTimeoutStatement);
                    }
                }
            }
        }
        super.setConnection(conn);
    }

    @Override
    public String getShortName() {
        //noinspection HardCodedStringLiteral
        return "oracle";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        //noinspection HardCodedStringLiteral
        return "Oracle";
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (databaseMajorVersion == null) {
            return super.getDatabaseMajorVersion();
        } else {
            return databaseMajorVersion;
        }
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        if (databaseMinorVersion == null) {
            return super.getDatabaseMinorVersion();
        } else {
            return databaseMinorVersion;
        }
    }

    @Override
    public Integer getDefaultPort() {
        return 1521;
    }

    @Override
    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        return correctObjectName((schema.getCatalogName() == null) ? schema.getSchemaName() : schema.getCatalogName(), Schema.class);
    }

    @Override
    protected String getAutoIncrementClause(final String generationType, final Boolean defaultOnNull) {
        if (StringUtil.isEmpty(generationType)) {
            return super.getAutoIncrementClause();
        }

        String autoIncrementClause = "GENERATED %s AS IDENTITY"; // %s -- [ ALWAYS | BY DEFAULT [ ON NULL ] ]
        String generationStrategy = generationType;
        if (Boolean.TRUE.equals(defaultOnNull) && generationType.toUpperCase().equals("BY DEFAULT")) {
            generationStrategy += " ON NULL";
        }
        return String.format(autoIncrementClause, generationStrategy);
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.length() > 27) {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US).substring(0, 27);
        } else {
            //noinspection HardCodedStringLiteral
            return "PK_" + tableName.toUpperCase(Locale.US);
        }
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return reservedWords.contains(objectName.toUpperCase());
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Schema.class.isAssignableFrom(object)) {
            return false;
        }
        return super.supports(object);
    }

    /**
     * Oracle supports catalogs in liquibase terms
     *
     * @return false
     */
    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    protected String getConnectionCatalogName() throws DatabaseException {
        if (getConnection() instanceof OfflineConnection) {
            return getConnection().getCatalog();
        }

        if (!(getConnection() instanceof JdbcConnection)) {
            return defaultCatalogName;
        }

        try {
            //noinspection HardCodedStringLiteral
            return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(new RawCallStatement("select sys_context( 'userenv', 'current_schema' ) from dual"), String.class);
        } catch (Exception e) {
            //noinspection HardCodedStringLiteral
            Scope.getCurrentScope().getLog(getClass()).info("Error getting default schema", e);
        }
        return null;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        //noinspection HardCodedStringLiteral
        if (url.startsWith("jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
    }

    @Override
    public String getDefaultCatalogName() {//NOPMD
        String defaultCatalogName = super.getDefaultCatalogName();
        if (Boolean.TRUE.equals(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())) {
            return defaultCatalogName;
        }
        return (defaultCatalogName == null) ? null : defaultCatalogName.toUpperCase(Locale.US);
    }

    /**
     * <p>Returns an Oracle date literal with the same value as a string formatted using ISO 8601.</p>
     *
     * <p>Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')</p>
     * <p>
     * Implementation restriction:<br>
     * Currently, only the following subsets of ISO8601 are supported:<br>
     * <ul>
     * <li>YYYY-MM-DD</li>
     * <li>YYYY-MM-DDThh:mm:ss</li>
     * </ul>
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            return "TO_DATE(" + normalLiteral + ", 'YYYY-MM-DD')";
        } else if (isTimeOnly(isoDate)) {
            return "TO_DATE(" + normalLiteral + ", 'HH24:MI:SS')";
        } else if (isTimestamp(isoDate)) {
            return "TO_TIMESTAMP(" + normalLiteral + ", 'YYYY-MM-DD HH24:MI:SS.FF')";
        } else if (isDateTime(isoDate)) {
            int seppos = normalLiteral.lastIndexOf('.');
            if (seppos != -1) {
                normalLiteral = normalLiteral.substring(0, seppos) + "'";
            }
            return "TO_DATE(" + normalLiteral + ", 'YYYY-MM-DD HH24:MI:SS')";
        }
        return "UNSUPPORTED:" + isoDate;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example == null) {
            return false;
        }

        if (this.isLiquibaseObject(example)) {
            return false;
        }

        if (example instanceof Schema) {
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if ("SYSTEM".equals(example.getName()) || "SYS".equals(example.getName()) || "CTXSYS".equals(example.getName()) || "XDB".equals(example.getName())) {
                return true;
            }
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if ("SYSTEM".equals(example.getSchema().getCatalogName()) || "SYS".equals(example.getSchema().getCatalogName()) || "CTXSYS".equals(example.getSchema().getCatalogName()) || "XDB".equals(example.getSchema().getCatalogName())) {
                return true;
            }
        } else if (isSystemObject(example.getSchema())) {
            return true;
        }
        if (example instanceof Catalog) {
            //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
            if (("SYSTEM".equals(example.getName()) || "SYS".equals(example.getName()) || "CTXSYS".equals(example.getName()) || "XDB".equals(example.getName()))) {
                return true;
            }
        } else if (example.getName() != null) {
            //noinspection HardCodedStringLiteral
            if (example.getName().startsWith("BIN$")) { //oracle deleted table
                boolean filteredInOriginalQuery = this.canAccessDbaRecycleBin();
                if (!filteredInOriginalQuery) {
                    filteredInOriginalQuery = StringUtil.trimToEmpty(example.getSchema().getName()).equalsIgnoreCase(this.getConnection().getConnectionUserName());
                }

                if (filteredInOriginalQuery) {
                    return !((example instanceof PrimaryKey) || (example instanceof Index) || (example instanceof
                            liquibase.statement.UniqueConstraint));
                } else {
                    return true;
                }
            } else //noinspection HardCodedStringLiteral
                if (example.getName().startsWith("AQ$")) { //oracle AQ tables
                    return true;
                } else //noinspection HardCodedStringLiteral
                    if (example.getName().startsWith("DR$")) { //oracle index tables
                        return true;
                    } else //noinspection HardCodedStringLiteral
                        if (example.getName().startsWith("SYS_IOT_OVER")) { //oracle system table
                            return true;
                        } else //noinspection HardCodedStringLiteral,HardCodedStringLiteral
                            if ((example.getName().startsWith("MDRT_") || example.getName().startsWith("MDRS_")) && example.getName().endsWith("$")) {
                                // CORE-1768 - Oracle creates these for spatial indices and will remove them when the index is removed.
                                return true;
                            } else //noinspection HardCodedStringLiteral
                                if (example.getName().startsWith("MLOG$_")) { //Created by materliaized view logs for every table that is part of a materialized view. Not available for DDL operations.
                                    return true;
                                } else //noinspection HardCodedStringLiteral
                                    if (example.getName().startsWith("RUPD$_")) { //Created by materialized view log tables using primary keys. Not available for DDL operations.
                                        return true;
                                    } else //noinspection HardCodedStringLiteral
                                        if (example.getName().startsWith("WM$_")) { //Workspace Manager backup tables.
                                            return true;
                                        } else //noinspection HardCodedStringLiteral
                                            if ("CREATE$JAVA$LOB$TABLE".equals(example.getName())) { //This table contains the name of the Java object, the date it was loaded, and has a BLOB column to store the Java object.
                                                return true;
                                            } else //noinspection HardCodedStringLiteral
                                                if ("JAVA$CLASS$MD5$TABLE".equals(example.getName())) { //This is a hash table that tracks the loading of Java objects into a schema.
                                                    return true;
                                                } else //noinspection HardCodedStringLiteral
                                                    if (example.getName().startsWith("ISEQ$$_")) { //System-generated sequence
                                                        return true;
                                                    } else //noinspection HardCodedStringLiteral
                                                        if (example.getName().startsWith("USLOG$")) { //for update materialized view
                                                            return true;
                                                        } else if (example.getName().startsWith("SYS_FBA")) { //for Flashback tables
                                                            return true;
                                                        }
        }

        return super.isSystemObject(example);
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean supportsAutoIncrement() {
        // Oracle supports Identity beginning with version 12c
        boolean isAutoIncrementSupported = false;

        try {
            if (getDatabaseMajorVersion() >= 12) {
                isAutoIncrementSupported = true;
            }

            // Returning true will generate create table command with 'IDENTITY' clause, example:
            // CREATE TABLE AutoIncTest (IDPrimaryKey NUMBER(19) GENERATED BY DEFAULT AS IDENTITY NOT NULL, TypeID NUMBER(3) NOT NULL, Description NVARCHAR2(50), CONSTRAINT PK_AutoIncTest PRIMARY KEY (IDPrimaryKey));

            // While returning false will continue to generate create table command without 'IDENTITY' clause, example:
            // CREATE TABLE AutoIncTest (IDPrimaryKey NUMBER(19) NOT NULL, TypeID NUMBER(3) NOT NULL, Description NVARCHAR2(50), CONSTRAINT PK_AutoIncTest PRIMARY KEY (IDPrimaryKey));

        } catch (DatabaseException ex) {
            isAutoIncrementSupported = false;
        }

        return isAutoIncrementSupported;
    }


    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        //noinspection HardCodedStringLiteral
        if ("BINARY_FLOAT".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        //noinspection HardCodedStringLiteral
        if ("BINARY_DOUBLE".equals(dataTypeName.toUpperCase())) {
            return 0;
        }
        return super.getDataTypeMaxParameters(dataTypeName);
    }

    public String getSystemTableWhereClause(String tableNameColumn) {
        List<String> clauses = new ArrayList<>(Arrays.asList("BIN$",
                "AQ$",
                "DR$",
                "SYS_IOT_OVER",
                "MLOG$_",
                "RUPD$_",
                "WM$_",
                "ISEQ$$_",
                "USLOG$",
                "SYS_FBA"));

        clauses.replaceAll(s -> tableNameColumn + " NOT LIKE '" + s + "%'");
        return "("+ StringUtil.join(clauses, " AND ") + ")";
    }

    @Override
    public boolean jdbcCallsCatalogsSchemas() {
        return true;
    }

    public Set<String> getUserDefinedTypes() {
        if (userDefinedTypes == null) {
            userDefinedTypes = new HashSet<>();
            if ((getConnection() != null) && !(getConnection() instanceof OfflineConnection)) {
                try {
                    try {
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForList(new RawParameterizedSqlStatement("SELECT DISTINCT TYPE_NAME FROM ALL_TYPES"), String.class));
                    } catch (DatabaseException e) { //fall back to USER_TYPES if the user cannot see ALL_TYPES
                        //noinspection HardCodedStringLiteral
                        userDefinedTypes.addAll(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForList(new RawParameterizedSqlStatement("SELECT TYPE_NAME FROM USER_TYPES"), String.class));
                    }
                } catch (DatabaseException e) {
                    //ignore error
                }
            }
        }

        return userDefinedTypes;
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        //noinspection HardCodedStringLiteral
        if ((databaseFunction != null) && "current_timestamp".equalsIgnoreCase(databaseFunction.toString())) {
            return databaseFunction.toString();
        }
        if ((databaseFunction instanceof SequenceNextValueFunction)
                || (databaseFunction instanceof SequenceCurrentValueFunction)) {
            String quotedSeq = super.generateDatabaseFunctionValue(databaseFunction);

            // replace "myschema.my_seq".nextval with "myschema"."my_seq".nextval
            return quotedSeq.replaceFirst("\"([^.\"]+)\\.([^.\"]+)\"", "\"$1\".\"$2\"");

        }

        return super.generateDatabaseFunctionValue(databaseFunction);
    }

    @Override
    public ValidationErrors validate() {
        ValidationErrors errors = super.validate();
        DatabaseConnection connection = getConnection();
        if ((connection == null) || (connection instanceof OfflineConnection)) {
            //noinspection HardCodedStringLiteral
            Scope.getCurrentScope().getLog(getClass()).info("Cannot validate offline database");
            return errors;
        }

        if (!canAccessDbaRecycleBin()) {
            errors.addWarning(getDbaRecycleBinWarning());
        }

        return errors;

    }

    public String getDbaRecycleBinWarning() {
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral,
        // HardCodedStringLiteral
        //noinspection HardCodedStringLiteral,HardCodedStringLiteral,HardCodedStringLiteral
        return "Liquibase needs to access the DBA_RECYCLEBIN table so we can automatically handle the case where " +
                "constraints are deleted and restored. Since Oracle doesn't properly restore the original table names " +
                "referenced in the constraint, we use the information from the DBA_RECYCLEBIN to automatically correct this" +
                " issue.\n" +
                "\n" +
                "The user you used to connect to the database (" + getConnection().getConnectionUserName() +
                ") needs to have \"SELECT ON SYS.DBA_RECYCLEBIN\" permissions set before we can perform this operation. " +
                "Please run the following SQL to set the appropriate permissions, and try running the command again.\n" +
                "\n" +
                "     GRANT SELECT ON SYS.DBA_RECYCLEBIN TO " + getConnection().getConnectionUserName() + ";";
    }

    public boolean canAccessDbaRecycleBin() {
        if (canAccessDbaRecycleBin == null) {
            DatabaseConnection connection = getConnection();
            if ((connection == null) || (connection instanceof OfflineConnection)) {
                return false;
            }

            Statement statement = null;
            try {
                statement = ((JdbcConnection) connection).createStatement();
                @SuppressWarnings("HardCodedStringLiteral") ResultSet resultSet = statement.executeQuery("select 1 from dba_recyclebin where 0=1");
                resultSet.close(); //don't need to do anything with the result set, just make sure statement ran.
                this.canAccessDbaRecycleBin = true;
            } catch (Exception e) {
                //noinspection HardCodedStringLiteral
                if ((e instanceof SQLException) && e.getMessage().startsWith("ORA-00942")) { //ORA-00942: table or view does not exist
                    this.canAccessDbaRecycleBin = false;
                } else {
                    //noinspection HardCodedStringLiteral
                    Scope.getCurrentScope().getLog(getClass()).warning("Cannot check dba_recyclebin access", e);
                    this.canAccessDbaRecycleBin = false;
                }
            } finally {
                JdbcUtil.close(null, statement);
            }
        }

        return canAccessDbaRecycleBin;
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return true;
    }

    /**
     * Tests if the given String would be a valid identifier in Oracle DBMS. In Oracle, a valid identifier has
     * the following form (case-insensitive comparison):
     * 1st character: A-Z
     * 2..n characters: A-Z0-9$_#
     * The maximum length of an identifier differs by Oracle version and object type.
     */
    public boolean isValidOracleIdentifier(String identifier, Class<? extends DatabaseObject> type) {
        if ((identifier == null) || (identifier.length() < 1))
            return false;

        if (!identifier.matches("^(i?)[A-Z][A-Z0-9\\$\\_\\#]*$"))
            return false;

        /*
         * @todo It seems we currently do not have a class for tablespace identifiers, and all other classes
         * we do know seem to be supported as 12cR2 long identifiers, so:
         */
        return (identifier.length() <= LONG_IDENTIFIERS_LEGNTH);
    }

    /**
     * Returns the maximum number of bytes (NOT: characters) for an identifier. For Oracle <=12c Release 20, this
     * is 30 bytes, and starting from 12cR2, up to 128 (except for tablespaces, PDB names and some other rather rare
     * object types).
     *
     * @return the maximum length of an object identifier, in bytes
     */
    public int getIdentifierMaximumLength() {
        try {
            if (getDatabaseMajorVersion() < ORACLE_12C_MAJOR_VERSION) {
                return SHORT_IDENTIFIERS_LENGTH;
            } else if ((getDatabaseMajorVersion() == ORACLE_12C_MAJOR_VERSION) && (getDatabaseMinorVersion() <= 1)) {
                return SHORT_IDENTIFIERS_LENGTH;
            } else {
                return LONG_IDENTIFIERS_LEGNTH;
            }
        } catch (DatabaseException ex) {
            throw new UnexpectedLiquibaseException("Cannot determine the Oracle database version number", ex);
        }

    }

    @Override
    public boolean supportsDatabaseChangeLogHistory() {
        return true;
    }

}
