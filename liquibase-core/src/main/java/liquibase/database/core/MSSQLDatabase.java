package liquibase.database.core;

import java.math.BigInteger;
import java.sql.ResultSet;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.LogFactory;
import liquibase.util.StringUtils;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("(?im)^\\s*(CREATE|ALTER)\\s+?VIEW\\s+?((\\S+?)|(\\[.*\\])|(\\\".*\\\"))\\s+?AS\\s+?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public String getShortName() {
        return "mssql";
    }

    public MSSQLDatabase() {
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
        systemTablesAndViews.add("sysdiagrams");

        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");
    }


    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "SQL Server";
    }

    public Integer getDefaultPort() {
        return 1433;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String databaseProductName = conn.getDatabaseProductName();
        return PRODUCT_NAME.equalsIgnoreCase(databaseProductName)
                || "SQLOLEDB".equalsIgnoreCase(databaseProductName);
    }

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
        if (getConnection() == null) {
            return null;
        }
        try {
            ResultSet resultSet = ((JdbcConnection) getConnection()).prepareStatement("select schema_name()").executeQuery();
            resultSet.next();
            return resultSet.getString(1);
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
        return "["+objectName+"]";
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
        try {
            return this.getDatabaseMajorVersion() > 10;
        } catch (DatabaseException e) {
            return true;
        }
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
          schema = correctSchema(schema);
        List<String> defLines = (List<String>) ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);
        StringBuffer sb = new StringBuffer();
        for (String defLine : defLines) {
            sb.append(defLine);
        }
        String definition = sb.toString();

        if (definition == null) {
            return null;
        }
        return CREATE_VIEW_AS_PATTERN.matcher(definition).replaceFirst("");
    }

    /**
     * SQLServer does not support specifying the database name as a prefix to the object name
     * @return
     */
    @Override
    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        if (!getOutputDefaultSchema() && !isDefaultSchema(catalogName, schemaName)) {
            return escapeObjectName(viewName, View.class);
        } else {
            return escapeObjectName(schemaName, Schema.class)+"."+ escapeObjectName(viewName, Schema.class);
        }

    }


}
