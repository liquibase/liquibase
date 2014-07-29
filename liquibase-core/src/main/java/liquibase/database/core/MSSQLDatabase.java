package liquibase.database.core;

import java.math.BigInteger;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.logging.LogFactory;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("(?im)^\\s*(CREATE|ALTER)\\s+VIEW\\s+(\\S+)\\s+?AS\\s*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public String getShortName() {
        return "mssql";
    }

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

        super.quotingStartCharacter ="[";
        super.quotingEndCharacter="]";
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
        return 1433;
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
            if (this.getDatabaseMajorVersion() >= 11) {
                return true;
            }
        } catch (DatabaseException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String databaseProductName = conn.getDatabaseProductName();
        return PRODUCT_NAME.equalsIgnoreCase(databaseProductName)
                || "SQLOLEDB".equalsIgnoreCase(databaseProductName);
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
    protected String getConnectionSchemaName() {
        if (getConnection() == null || getConnection() instanceof OfflineConnection) {
            return null;
        }
        try {
            return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("select schema_name()"), String.class);
        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
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
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        // MSSQL server does not support the schema name for the index -
        return super.escapeObjectName(indexName, Index.class);
    }

    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return escapeObjectName(null, schemaName, tableName, Table.class);
    }

    //    protected void dropForeignKeys(Connection conn) throws DatabaseException {
//        Statement dropStatement = null;
//        PreparedStatement fkStatement = null;
//        ResultSet rs = null;
//        try {
//            dropStatement = conn.createStatement();
//
//            fkStatement = conn.prepareStatement("select TABLE_NAME, CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY' AND TABLE_CATALOG=?");
//            fkStatement.setString(1, getDefaultCatalogName());
//            rs = fkStatement.executeQuery();
//            while (rs.next()) {
//                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
//                dropFK.setBaseTableName(rs.getString("TABLE_NAME"));
//                dropFK.setConstraintName(rs.getString("CONSTRAINT_NAME"));
//
//                try {
//                    dropStatement.execute(dropFK.generateStatements(this)[0]);
//                } catch (UnsupportedChangeException e) {
//                    throw new DatabaseException(e.getMessage());
//                }
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        } finally {
//            try {
//                if (dropStatement != null) {
//                    dropStatement.close();
//                }
//                if (fkStatement != null) {
//                    fkStatement.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            } catch (SQLException e) {
//                throw new DatabaseException(e);
//            }
//        }
//
//    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example.getSchema() == null || example.getSchema().getName() == null) {
            return super.isSystemObject(example);
        }

        if (example instanceof Table && example.getSchema().getName().equals("sys")) {
            return true;
        }
        if (example instanceof View && example.getSchema().getName().equals("sys")) {
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

        if (objectName.contains("(")) { //probably a function
            return objectName;
        }
        return this.quotingStartCharacter+objectName+this.quotingEndCharacter;
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
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
          schema = schema.customize(this);
        List<String> defLines = (List<String>) ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        StringBuffer sb = new StringBuffer();
        for (String defLine : defLines) {
            sb.append(defLine);
        }
        String definition = sb.toString();

        String finalDef =definition.replaceAll("\\r\\n", "\n").trim();

        String selectOnly = CREATE_VIEW_AS_PATTERN.matcher(finalDef).replaceFirst("");
        if (selectOnly.equals(finalDef)) {
            return "FULL_DEFINITION: " + finalDef;
        }

        selectOnly = selectOnly.trim();


        /**handle views that end up as '(select XYZ FROM ABC);' */
        if (selectOnly.startsWith("(") && (selectOnly.endsWith(")") || selectOnly.endsWith(");"))) {
            selectOnly = selectOnly.replaceFirst("^\\(", "");
            selectOnly = selectOnly.replaceFirst("\\);?$", "");
        }

        return selectOnly;
    }

    /**
     * SQLServer does not support specifying the database name as a prefix to the object name
     * @return
     */
    @Override
    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return escapeObjectName(null, schemaName, viewName, View.class);

    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        String schemaName = super.getJdbcSchemaName(schema);
        if (schemaName != null) {
            schemaName = schemaName.toLowerCase();
        }
        return schemaName;
    }

    @Override
    public boolean isCaseSensitive() {

        if (caseSensitive == null) {
            try {
                if (getConnection() != null) {
                    String collation = ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("SELECT CONVERT(varchar(100), SERVERPROPERTY('COLLATION'))"), String.class);
                    caseSensitive = ! collation.contains("_CI_");
                }
            } catch (Exception e) {
                LogFactory.getLogger().warning("Cannot determine case sensitivity from MSSQL", e);
            }
        }

        if (caseSensitive == null) {
            return false;
        } else {
            return caseSensitive.booleanValue();
        }
    }
}
