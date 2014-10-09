package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates Sybase ASE database support.
 */
public class SybaseDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "Adaptive Server Enterprise";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    @Override
    public String getShortName() {
        return "sybase";
    }

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

        super.quotingStartCharacter ="[";
        super.quotingEndCharacter="]";

    }

/*    public void setConnection(Connection connection) {
        super.setConnection(new SybaseConnectionDelegate(connection));
    }
    */

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
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
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

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example.getSchema() != null && example.getSchema().getName() != null) {
            if (example instanceof Table && (example.getSchema().getName().equals("sys") || example.getSchema().getName().equals("sybfi"))) {
                return true;
            }
            if (example instanceof View && (example.getSchema().getName().equals("sys") || example.getSchema().getName().equals("sybfi"))) {
                return true;
            }
        }
        return super.isSystemObject(example);
    }


    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }

    @Override
    protected String getConnectionSchemaName() {
        if (getConnection() == null || getConnection() instanceof OfflineConnection) {
            return null;
        }
        try {
            return ExecutorService.getInstance().getExecutor(this).queryForObject(new RawSqlStatement("select user_name()"), String.class);
        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
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
	 * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMajorVersion()
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
	 * @see liquibase.database.AbstractJdbcDatabase#getDatabaseMinorVersion()
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
    public String escapeIndexName(String catalogName,String schemaName, String indexName) {
        return indexName;
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
}
