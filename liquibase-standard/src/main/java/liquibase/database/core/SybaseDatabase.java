package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.util.StringUtil;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates Sybase ASE database support.
 */
public class SybaseDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Adaptive Server Enterprise";
    protected Set<String> systemTablesAndViews = new HashSet<>();

    public SybaseDatabase() {
        super.setCurrentDateTimeFunction("GETDATE()");
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
        systemTablesAndViews.add("sysquerymetrics");
        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");
    }

    @Override
    public String getShortName() {
        return "sybase";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    @Override
    public Integer getDefaultPort() {
        return 4100;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Sybase";
    }

    /**
     * Sybase does not support DDL and meta data in transactions properly,
     * as such we turn off the commit and turn on auto commit.
     */
    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supports(Class<? extends DatabaseObject> object) {
        if (Sequence.class.isAssignableFrom(object)) {
            return false;
        }
        return super.supports(object);
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String dbProductName = conn.getDatabaseProductName();
        return isSybaseProductName(dbProductName);
    }

    // package private to facilitate testing
    boolean isSybaseProductName(String dbProductName) {
        return
                PRODUCT_NAME.equals(dbProductName)
                        || "Sybase SQL Server".equals(dbProductName)
                        || "sql server".equals(dbProductName)
                        || "ASE".equals(dbProductName);
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:xsybase") || url.startsWith("jdbc:sybase:Tds")) {
            return "com.sybase.jdbc4.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:jtds:sybase")) {
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
        // not supported
        return false;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        // not supported
        return false;
    }

    @Override
    public String getConcatSql(String... values) {
        return StringUtil.join(values, " + ");
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if ((example.getSchema() != null) && (example.getSchema().getName() != null)) {
            if ((example instanceof Table) && ("sys".equals(example.getSchema().getName()) || "sybfi".equals(example
                    .getSchema().getName()))) {
                return true;
            }
            if ((example instanceof View) && ("sys".equals(example.getSchema().getName()) || "sybfi".equals(example
                    .getSchema().getName()))) {
                return true;
            }
        }

        if (example instanceof Column && example.getName().startsWith("sybfi")) {
            return true;
        }

        return super.isSystemObject(example);
    }


    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawParameterizedSqlStatement("select user_name()");
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
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        schema = schema.customize(this);
        GetViewDefinitionStatement statement = new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName);
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this);
        @SuppressWarnings("unchecked")
        List<String> definitionRows = (List<String>) executor.queryForList(statement, String.class);
        StringBuilder definition = new StringBuilder();
        for (String d : definitionRows) {
            definition.append(d);
        }
        /*delete the words "CREATE VIEW [name_view] as"*/
        String defUpper = definition.toString().toUpperCase();
        int findPos = 0;
        if (defUpper.contains(" AS ")) {
            findPos = defUpper.indexOf(" AS ") + 3;
        } else if (defUpper.contains(" AS\n")) {
            findPos = defUpper.indexOf(" AS\n") + 3;
        } else if (defUpper.contains("\nAS ")) {
            findPos = defUpper.indexOf("\nAS ") + 3;
        } else if (defUpper.contains("\nAS\n")) {
            findPos = defUpper.indexOf("\nAS\n") + 3;
        }
        return definition.substring(findPos, definition.toString().length());
    }

    /**
     * @return the major version if supported, otherwise -1
     * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMajorVersion()
     */
    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (getConnection() == null) {
            return -1;
        }

        return getConnection().getDatabaseMajorVersion();
    }

    /**
     * @return the minor version if supported, otherwise -1
     * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMinorVersion()
     */
    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        if (getConnection() == null) {
            return -1;
        }

        return getConnection().getDatabaseMinorVersion();
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return indexName;
    }

    @Override
    public String quoteObject(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName == null) {
            return null;
        }
        return getQuotingStartCharacter() + objectName + getQuotingEndCharacter();
    }

    @Override
    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectName.contains("(")) { //probably a function
            return false;
        }

        return super.mustQuoteObjectName(objectName, objectType);
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
    public boolean requiresExplicitNullForColumns() {
        /* SAP Adaptive Server Enterprise and, by extension, SQL Anywhere in ASE compatibility mode have the
         * strange requirement of setting the nullability of a column to NOT NULL if neither NULL nor
         * NOT NULL are specified. See:
         * http://dcx.sap.com/index.html#sqla170/en/html/819378356ce21014a17f8d51529119ee.html
         */
        return true;
    }
}
