package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.statement.core.GetViewDefinitionStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates Sybase ASE database support.
 */
public class SybaseDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Adaptive Server Enterprise";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    public String getProductName() {
        return "Sybase SQL Server";
    }

    public String getTypeName() {
        return "sybase";
    }

    public SybaseDatabase() {
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

/*    public void setConnection(Connection connection) {
        super.setConnection(new SybaseConnectionDelegate(connection));
    }
    */

    public int getPriority() {
        return PRIORITY_DEFAULT;
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
    public Set<String> getSystemTablesAndViews() {
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
        String dbProductName = conn.getDatabaseProductName();
        return isSybaseProductName(dbProductName);
    }

    // package private to facilitate testing
    boolean isSybaseProductName(String dbProductName) {
        return
                "Adaptive Server Enterprise".equals(dbProductName)
                || "Sybase SQL Server".equals(dbProductName)
                || "sql server".equals(dbProductName)
                || "ASE".equals(dbProductName);
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:jtds:sybase")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "GETDATE()";
    }

    @Override
    public String getAutoIncrementClause() {
        return "IDENTITY";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
        return null;
    }

    @Override
    public String getDefaultCatalogName() throws DatabaseException {
        return getConnection().getCatalog();
    }

    @Override
    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        for (String value : values) {
            returnString.append(value).append(" + ");
        }

        return returnString.toString().replaceFirst(" \\+ $", "");
    }

//    protected void dropForeignKeys(Connection conn) throws JDBCException {
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
//                    throw new JDBCException(e.getMessage());
//                }
//            }
//        } catch (SQLException e) {
//            throw new JDBCException(e);
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
//                throw new JDBCException(e);
//            }
//        }
//
//    }

    public boolean supportsTablespaces() {
        return true;
    }


    @Override
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return super.isSystemTable(catalogName, schemaName, tableName) || schemaName.equals("sys") || tableName.toLowerCase().startsWith("sybfi");
    }

    @Override
    public boolean isSystemView(String catalogName, String schemaName, String viewName) {
        return super.isSystemView(catalogName, schemaName, viewName) || schemaName.equals("sys") || viewName.toLowerCase().equals("sybfi");
    }

    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }


    @Override
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return getDefaultCatalogName();
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            requestedSchema = getDefaultDatabaseSchemaName();
        }

        if (requestedSchema == null) {
            return "dbo";
        }
        return requestedSchema;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public String escapeDatabaseObject(String objectName) {
        return "["+objectName+"]";
    }

	@Override
	public String getViewDefinition(String schemaName, String viewName) throws DatabaseException {
        GetViewDefinitionStatement statement = new GetViewDefinitionStatement(convertRequestedSchemaToSchema(schemaName), viewName);
        Executor executor = ExecutorService.getInstance().getExecutor(this);
        @SuppressWarnings("unchecked")
        List<String> definitionRows = (List<String>) executor.queryForList(statement, String.class);
        StringBuilder definition = new StringBuilder();
        for (String d : definitionRows) {
        	definition.append(d);
        }
        return definition.toString();
	}
	
	/** 
	 * @return the major version if supported, otherwise -1
	 * @see liquibase.database.AbstractDatabase#getDatabaseMajorVersion()
	 */
	@Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        try {
            return getConnection().getDatabaseMajorVersion();
        } catch (UnsupportedOperationException e) {
        	LogFactory.getLogger()
        		.warning("Your JDBC driver does not support getDatabaseMajorVersion(). Consider upgrading it.");
            return -1;
        }
    }

	/**
	 * @return the minor version if supported, otherwise -1
	 * @see liquibase.database.AbstractDatabase#getDatabaseMinorVersion()
	 */
	@Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        try {
            return getConnection().getDatabaseMinorVersion();
        } catch (UnsupportedOperationException e) {
        	LogFactory.getLogger()
    			.warning("Your JDBC driver does not support getDatabaseMajorVersion(). Consider upgrading it.");
            return -1;
        }
    }

    @Override
    public String escapeIndexName(String schemaName, String indexName) {
        return super.escapeIndexName(null, indexName);
    }

}
