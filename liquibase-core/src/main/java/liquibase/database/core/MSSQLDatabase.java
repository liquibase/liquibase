package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";

    public static final class MSSQL_SERVER_VERSIONS {
        public static final int MSSQL2008 = 10;
        public static final int MSSQL2012 = 11;
        public static final int MSSQL2014 = 12;
        public static final int MSSQL2016 = 13;
        public static final int MSSQL2017 = 14;

        private MSSQL_SERVER_VERSIONS() {
            throw new IllegalStateException("this class is not expected to be instantiated.");
        }
    }

    private HashMap<String, Integer> defaultDataTypeParameters = new HashMap<>();

    protected static final int MSSQL_DEFAULT_TCP_PORT = 1433;

    private static final Pattern CREATE_VIEW_AS_PATTERN =
        Pattern.compile(
            "(?im)^\\s*(CREATE|ALTER)\\s+VIEW\\s+(\\S+)\\s+?AS\\s*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

    protected Set<String> systemTablesAndViews = new HashSet<>();

    private Boolean sendsStringParametersAsUnicode;

    // "Magic numbers" are ok here because we populate a lot of self-explaining metadata.
    @SuppressWarnings("squid:S109")
    public MSSQLDatabase() {
        super.setCurrentDateTimeFunction("GETDATE()");

        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";

        systemTablesAndViews.add("syscolumns");
        systemTablesAndViews.add("syscomments");
        systemTablesAndViews.add("sysdepends");
        systemTablesAndViews.add("sysfilegroups");
        systemTablesAndViews.add("sysfiles");
        systemTablesAndViews.add("sysfiles1");
        systemTablesAndViews.add("sysforeignkeys");
        systemTablesAndViews.add("sysfulltextcatalogs");
        systemTablesAndViews.add("sysfulltextnotify");
        systemTablesAndViews.add("sysindexes");
        systemTablesAndViews.add("sysindexkeys");
        systemTablesAndViews.add("sysmembers");
        systemTablesAndViews.add("sysobjects");
        systemTablesAndViews.add("syspermissions");
        systemTablesAndViews.add("sysproperties");
        systemTablesAndViews.add("sysprotects");
        systemTablesAndViews.add("sysreferences");
        systemTablesAndViews.add("systypes");
        systemTablesAndViews.add("sysusers");
        systemTablesAndViews.add("sysdiagrams");
        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");

        // Information obtained from:
        // https://docs.microsoft.com/en-us/sql/t-sql/data-types/precision-scale-and-length-transact-sql
        defaultDataTypeParameters.put("datetime", 3);
        defaultDataTypeParameters.put("datetime2", 7);
        defaultDataTypeParameters.put("datetimeoffset", 7);
        defaultDataTypeParameters.put("time", 7);
        defaultDataTypeParameters.put("decimal", 0);
        defaultDataTypeParameters.put("numeric", 0);
        defaultDataTypeParameters.put("bigint", 0);
        defaultDataTypeParameters.put("int", 0);
        defaultDataTypeParameters.put("smallint", 0);
        defaultDataTypeParameters.put("tinyint", 0);
        defaultDataTypeParameters.put("money", 4);
        defaultDataTypeParameters.put("smallmoney", 0);


        // JDBC Driver version 6.2.0 does not seem to return this keyword, which causes an integration test to fail.
        addReservedWords(Arrays.asList("KEY"));
    }

    @Override
    public Integer getDefaultScaleForNativeDataType(String nativeDataType) {
        return defaultDataTypeParameters.get(nativeDataType.toLowerCase());
    }

    @Override
    public void setDefaultSchemaName(String schemaName) {
        if (schemaName != null && !schemaName.equalsIgnoreCase(getConnectionSchemaName())) {
            throw new RuntimeException(String.format(
                "Cannot use default schema name %s on Microsoft SQL Server because the login " +
                    "schema of the current user (%s) is different and MSSQL does not support " +
                    "setting the default schema per session.", schemaName, getConnectionSchemaName()));
        }
        super.setDefaultSchemaName(schemaName);
    }

    @Override
    public String getShortName() {
        return "mssql";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "SQL Server";
    }

    @Override
    public Integer getDefaultPort() {
        return MSSQL_DEFAULT_TCP_PORT;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    protected Set<String> getSystemTables() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        try {
            if (isAzureDb() || this.getDatabaseMajorVersion() >= MSSQL_SERVER_VERSIONS.MSSQL2012) {
                return true;
            }
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String databaseProductName = conn.getDatabaseProductName();
        int majorVersion = conn.getDatabaseMajorVersion();
        boolean isRealSqlServerConnection = PRODUCT_NAME.equalsIgnoreCase(databaseProductName)
                || "SQLOLEDB".equalsIgnoreCase(databaseProductName);

        if (isRealSqlServerConnection && (majorVersion <= MSSQL_SERVER_VERSIONS.MSSQL2008)) {
            LogService.getLog(getClass()).warning(
                LogType.LOG, String.format("Your SQL Server major version (%d) seems to indicate that your " +
                        "software is older than SQL Server 2008. Unfortunately, this is not supported, and this " +
                        "connection cannot be used.",
                 majorVersion));
            return false;
        }
        return isRealSqlServerConnection;
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:jtds:sqlserver")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    @Override
    protected String getAutoIncrementClause() {
        return "IDENTITY";
    }
    
    @Override
    protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
        return true;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        return true;
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
    public String getDefaultCatalogName() {
        if (getConnection() == null) {
            return null;
        }
        try {
            return getConnection().getCatalog();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawSqlStatement("select schema_name()");
    }

    @Override
    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" + ");
        }

        return returnString.toString().replaceFirst(" \\+ $", "");
    }

    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return escapeObjectName(catalogName, schemaName, tableName, Table.class);
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if ((example.getSchema() == null) || (example.getSchema().getName() == null)) {
            return super.isSystemObject(example);
        }

        if ((example instanceof Table) && "sys".equals(example.getSchema().getName())) {
            return true;
        }
        if ((example instanceof View) && "sys".equals(example.getSchema().getName())) {
            return true;
        }
        return super.isSystemObject(example);
    }

    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }


    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }

        if (objectName.contains("(")) {
            // probably a function
            return objectName;
        }

        return super.escapeObjectName(objectName, objectType);
    }

    @Override
    public String getDateLiteral(String isoDate) {
        return super.getDateLiteral(isoDate).replace(' ', 'T');
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        if (View.class.isAssignableFrom(type)) {
            // Microsoft SQL Server does not allow a catalog name in the CREATE ... VIEW statement:
            // https://docs.microsoft.com/en-gb/sql/t-sql/statements/create-view-transact-sql
            return false;
        } else {
            return Relation.class.isAssignableFrom(type);
        }
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        schema = schema.customize(this);
        List<String> defLines = (List<String>) ExecutorService.getInstance().getExecutor(this)
            .queryForList(
                new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName),
                String.class
            );
        StringBuffer sb = new StringBuffer();
        for (String defLine : defLines) {
            sb.append(defLine);
        }
        String definition = sb.toString();

        String finalDef =definition.replaceAll("\\r\\n", "\n").trim();

        // Keep comment as beginning of statement:
        if (finalDef.startsWith("--") || finalDef.startsWith("/*")) {
            return "FULL_DEFINITION: " + finalDef;
        }

        String selectOnly = CREATE_VIEW_AS_PATTERN.matcher(finalDef).replaceFirst("");
        if (selectOnly.equals(finalDef)) {
            return "FULL_DEFINITION: " + finalDef;
        }

        selectOnly = selectOnly.trim();


        // handle views that end up as '(select XYZ FROM ABC);'
        if (selectOnly.startsWith("(") && (selectOnly.endsWith(")") || selectOnly.endsWith(");"))) {
            selectOnly = selectOnly.replaceFirst("^\\(", "");
            selectOnly = selectOnly.replaceFirst("\\);?$", "");
        }

        return selectOnly;
    }

    @Override
    public String escapeObjectName(String catalogName, String schemaName, String objectName,
                                   Class<? extends DatabaseObject> objectType) {
        if (Index.class.isAssignableFrom(objectType)) {
            return super.escapeObjectName(objectName, objectType);
        }

        if ((catalogName != null) && !catalogName.equalsIgnoreCase(this.getDefaultCatalogName())) {
            return super.escapeObjectName(catalogName, schemaName, objectName, objectType);
        } else {
            String name = this.escapeObjectName(objectName, objectType);
            if (StringUtil.isEmpty(schemaName)) {
                schemaName = this.getDefaultSchemaName();
            }
            if ((!StringUtil.isEmpty(schemaName) && (!schemaName.equals(getConnectionSchemaName())))) {
                name = this.escapeObjectName(schemaName, Schema.class)+"."+name;
            }
            return name;
        }
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        String schemaName = super.getJdbcSchemaName(schema);
        if ((schemaName != null) && !isCaseSensitive()) {
            schemaName = schemaName.toLowerCase();
        }
        return schemaName;
    }

    @Override
    public boolean isCaseSensitive() {
        if (caseSensitive == null) {
            try {
                if (getConnection() instanceof JdbcConnection) {
                    String catalog = getConnection().getCatalog();
                    String sql =
                        "SELECT CONVERT([sysname], DATABASEPROPERTYEX(N'" + escapeStringForDatabase(catalog) +
                            "', 'Collation'))";
                    String collation = ExecutorService.getInstance().getExecutor(this)
                        .queryForObject(new RawSqlStatement(sql), String.class);
                    caseSensitive = (collation != null) && !collation.contains("_CI_");
                } else if (getConnection() instanceof OfflineConnection) {
                    caseSensitive = ((OfflineConnection) getConnection()).isCaseSensitive();
                }
            } catch (DatabaseException e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Cannot determine case sensitivity from MSSQL", e);
            }
        }
        return (caseSensitive != null) && caseSensitive;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        if ("bigint".equalsIgnoreCase(dataTypeName)
                || "bit".equalsIgnoreCase(dataTypeName)
                || "date".equalsIgnoreCase(dataTypeName)
                || "datetime".equalsIgnoreCase(dataTypeName)
                || "geography".equalsIgnoreCase(dataTypeName)
                || "geometry".equalsIgnoreCase(dataTypeName)
                || "hierarchyid".equalsIgnoreCase(dataTypeName)
                || "image".equalsIgnoreCase(dataTypeName)
                || "int".equalsIgnoreCase(dataTypeName)
                || "money".equalsIgnoreCase(dataTypeName)
                || "ntext".equalsIgnoreCase(dataTypeName)
                || "real".equalsIgnoreCase(dataTypeName)
                || "smalldatetime".equalsIgnoreCase(dataTypeName)
                || "smallint".equalsIgnoreCase(dataTypeName)
                || "smallmoney".equalsIgnoreCase(dataTypeName)
                || "text".equalsIgnoreCase(dataTypeName)
                || "timestamp".equalsIgnoreCase(dataTypeName)
                || "tinyint".equalsIgnoreCase(dataTypeName)
                || "rowversion".equalsIgnoreCase(dataTypeName)
                || "sql_variant".equalsIgnoreCase(dataTypeName)
                || "sysname".equalsIgnoreCase(dataTypeName)
                || "uniqueidentifier".equalsIgnoreCase(dataTypeName)) {

            return 0;
        }

        if ("binary".equalsIgnoreCase(dataTypeName)
                || "char".equalsIgnoreCase(dataTypeName)
                || "datetime2".equalsIgnoreCase(dataTypeName)
                || "datetimeoffset".equalsIgnoreCase(dataTypeName)
                || "float".equalsIgnoreCase(dataTypeName)
                || "nchar".equalsIgnoreCase(dataTypeName)
                || "nvarchar".equalsIgnoreCase(dataTypeName)
                || "time".equalsIgnoreCase(dataTypeName)
                || "varbinary".equalsIgnoreCase(dataTypeName)
                || "varchar".equalsIgnoreCase(dataTypeName)
                || "xml".equalsIgnoreCase(dataTypeName)) {

            return 1;
        }

        return 2;
    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        int indexOfPeriod = dataTypeName.indexOf('.');

        if (indexOfPeriod < 0) {
            if (!dataTypeName.startsWith(getQuotingStartCharacter())) {
                dataTypeName = escapeObjectName(dataTypeName, DatabaseObject.class);
            }

            return dataTypeName;
        }

        String schemaName = dataTypeName.substring(0, indexOfPeriod);
        if (!schemaName.startsWith(getQuotingStartCharacter())) {
            schemaName = escapeObjectName(schemaName, Schema.class);
        }

        dataTypeName = dataTypeName.substring(indexOfPeriod + 1, dataTypeName.length());
        if (!dataTypeName.startsWith(getQuotingStartCharacter())) {
            dataTypeName = escapeObjectName(dataTypeName, DatabaseObject.class);
        }

        return schemaName + "." + dataTypeName;
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        int indexOfPeriod = dataTypeName.indexOf('.');

        if (indexOfPeriod < 0) {
            if (dataTypeName.matches("\\[[^]\\[]++\\]")) {
                dataTypeName = dataTypeName.substring(1, dataTypeName.length() - 1);
            }

            return dataTypeName;
        }

        String schemaName = dataTypeName.substring(0, indexOfPeriod);
        if (schemaName.matches("\\[[^]\\[]++\\]")) {
            schemaName = schemaName.substring(1, schemaName.length() - 1);
        }

        dataTypeName = dataTypeName.substring(indexOfPeriod + 1, dataTypeName.length());
        if (dataTypeName.matches("\\[[^]\\[]++\\]")) {
            dataTypeName = dataTypeName.substring(1, dataTypeName.length() - 1);
        }

        return schemaName + "." + dataTypeName;
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        int indexOfLeftParen = dataTypeString.indexOf('(');
        if (indexOfLeftParen < 0) {
            return unescapeDataTypeName(dataTypeString);
        }

        return unescapeDataTypeName(dataTypeString.substring(0, indexOfLeftParen))
                + dataTypeString.substring(indexOfLeftParen);
    }

    /**
     * Determines if the SQL Server instance assigns Unicode data types (e.g. nvarchar) to strings.
     *
     * @return true if the SQL Server instance uses Unicode types by default, false if not.
     */
    public boolean sendsStringParametersAsUnicode() {
        if (sendsStringParametersAsUnicode == null) {
            try {
                if (getConnection() instanceof JdbcConnection) {
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        String sql = "SELECT CONVERT([sysname], SQL_VARIANT_PROPERTY(?, 'BaseType'))";
                        ps = ((JdbcConnection) getConnection()).prepareStatement(sql);
                        ps.setString(1, "Liquibase");
                        rs = ps.executeQuery();
                        String baseType = null;
                        if (rs.next()) {
                            baseType = rs.getString(1);
                        }
                        // baseTypes starting with "n" can be something like nvarchar (or nchar)
                        sendsStringParametersAsUnicode =
                            (baseType == null) || baseType.startsWith("n");
                    } finally {
                        JdbcUtils.close(rs, ps);
                    }
                } else if (getConnection() instanceof OfflineConnection) {
                    sendsStringParametersAsUnicode =
                        ((OfflineConnection) getConnection()).getSendsStringParametersAsUnicode();
                }
            } catch (SQLException | DatabaseException e) {
                LogService.getLog(getClass()).warning(
                    LogType.LOG, "Cannot determine whether String parameters are sent as Unicode for MSSQL", e);
            }
        }

        return (sendsStringParametersAsUnicode == null) ? true : sendsStringParametersAsUnicode;
    }

    /**
     * Returns true if the connected MS SQL instance is a Microsoft Cloud ("Azure")-hosted instance of MSSQL.
     * @return true if instance runs in Microsoft Azure, false otherwise
     */
    public boolean isAzureDb() {
        return "Azure".equalsIgnoreCase(getEngineEdition());
    }

    /**
     * Determines the capabilities ("Edition") of the SQL Server database. Possible values are currently
     * "Personal", "Standard", "Enterprise" (Developer Edition is also reported as Enterprise), "Express" or "Azure".
     *
     * @return one of the strings above
     */
    public String getEngineEdition() {
        try {
            if (getConnection() instanceof JdbcConnection) {
                String sql = "SELECT CASE ServerProperty('EngineEdition')\n" +
                    "         WHEN 1 THEN 'Personal'\n" +
                    "         WHEN 2 THEN 'Standard'\n" +
                    "         WHEN 3 THEN 'Enterprise'\n" +
                    "         WHEN 4 THEN 'Express'\n" +
                    "         WHEN 5 THEN 'Azure'\n" +
                    "         ELSE 'Unknown'\n" +
                    "       END";
                return ExecutorService.getInstance().getExecutor(this)
                    .queryForObject(new RawSqlStatement(sql), String.class);
            }
        } catch (DatabaseException e) {
            LogService.getLog(getClass()).warning(LogType.LOG, "Could not determine engine edition", e);
        }
        return "Unknown";
    }

    @Override
    protected String getQuotingStartCharacter() {
        return "[";
    }

    @Override
    protected String getQuotingEndCharacter() {
        return "]";
    }

    @Override
    protected String getQuotingEndReplacement() {
        return "]]";
    }
}
