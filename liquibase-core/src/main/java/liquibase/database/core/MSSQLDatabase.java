package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public String getTypeName() {
        return "mssql";
    }

    public MSSQLDatabase() {
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

        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");
    }


    public int getPriority() {
        return PRIORITY_DEFAULT;
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

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }

        return "GETDATE()";
    }

    @Override
    protected String getAutoIncrementClause() {
    	return "IDENTITY";
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

    @Override
    public String escapeIndexName(String schemaName, String indexName) {
        // MSSQL server does not support the schema name for the index -
        return super.escapeIndexName(null, indexName);
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
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return super.isSystemTable(catalogName, schemaName, tableName) || schemaName.equals("sys");
    }

    @Override
    public boolean isSystemView(String catalogName, String schemaName, String viewName) {
        return super.isSystemView(catalogName, schemaName, viewName) || schemaName.equals("sys");
    }

    public String generateDefaultConstraintName(String tableName, String columnName) {
        return "DF_" + tableName + "_" + columnName;
    }


    @Override
    public String escapeDatabaseObject(String objectName) {
        return "["+objectName+"]";
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
	public String getDefaultSchemaName() {
        String defaultSchemaName = super.getDefaultSchemaName();
        if (defaultSchemaName == null) {
            return "dbo";
        } else {
            return defaultSchemaName;
        }

	}

      @Override
    public String getViewDefinition(String schemaName, String viewName) throws DatabaseException {
        if (schemaName == null) {
            schemaName = convertRequestedSchemaToSchema(null);
        }
        List<String> defLines = (List<String>) ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schemaName, viewName), String.class);
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
}
