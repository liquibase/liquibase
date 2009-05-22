package liquibase.database;

import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.MSSQLDatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates MS-SQL database support.
 */
public class MSSQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "Microsoft SQL Server";
    protected Set<String> systemTablesAndViews = new HashSet<String>();
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);
    private static final DataType DATE_TYPE = new DataType("SMALLDATETIME", false);
    private static final DataType BOOLEAN_TYPE = new DataType("BIT", false);
    private static final DataType CURRENCY_TYPE = new DataType("MONEY", false);
    private static final DataType UUID_TYPE = new DataType("UNIQUEIDENTIFIER", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType BLOB_TYPE = new DataType("IMAGE", true);

    public String getProductName() {
        return "Microsoft SQL";
    }

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

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:jtds:sqlserver")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }


    @Override
    public DataType getTimeType() {
        return DATETIME_TYPE;
    }


    @Override
    public DataType getDateType() {
        return DATE_TYPE;
    }

    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    public DataType getClobType() {
        return CLOB_TYPE;
    }

    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    public String getCurrentDateTimeFunction() {
        return "GETDATE()";
    }

    @Override
    public String getAutoIncrementClause() {
        return "IDENTITY";
    }
    @Override
    protected String getDefaultDatabaseSchemaName() throws JDBCException {
        return null;
    }

    @Override
    public String getDefaultCatalogName() throws JDBCException {
        try {
            return getConnection().getCatalog();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
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
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue == null) {
            return null;
        }

        if (defaultValue instanceof String) {
            if (((String) defaultValue).startsWith("('")) {
                defaultValue = ((String) defaultValue).replaceFirst("^\\('", "").replaceFirst("'\\)$", "");
            } else if (((String) defaultValue).startsWith("((")) {
                defaultValue = ((String) defaultValue).replaceFirst("^\\(\\(", "").replaceFirst("\\)\\)$", "");
            }
        }

        defaultValue = super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits);

        return defaultValue;
    }

    @Override
    public String escapeDatabaseObject(String objectName) {
        return "["+objectName+"]";
    }

    @Override
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
        return getDefaultCatalogName();
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        if (requestedSchema == null && getConnection() != null) {
            return getDefaultCatalogName();
        }
        return requestedSchema;
    }

    @Override
    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        String sql = "select view_definition from INFORMATION_SCHEMA.VIEWS where upper(table_name)='" + viewName.toUpperCase() + "'";
//        if (StringUtils.trimToNull(schemaName) != null) {
        sql += " and table_schema='" + convertRequestedSchemaToSchema(schemaName) + "'";
        sql += " and table_catalog='" + getDefaultCatalogName() + "'";
//        }

//        log.info("GetViewDefinitionSQL: "+sql);
        return new RawSqlStatement(sql);
    }

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (autoIncrement != null && autoIncrement) {
            type = type.replaceFirst(" identity$", "");
        }
        return type;
    }

    @Override
    public String getDateLiteral(String isoDate) {
        return super.getDateLiteral(isoDate).replace(' ', 'T');
    }

    @Override
    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new MSSQLDatabaseSnapshot(this, statusListeners, schema);
    }

	@Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

	@Override
	public String getDefaultSchemaName() {
        try {
        	DatabaseConnection connection = getConnection(); 
            return connection == null ? null: connection.getCatalog();
        } catch (SQLException e) {
            log.severe(e.getMessage());
            e.printStackTrace();
            return null;
        }
	}

	@Override
	protected SqlStatement getCreateChangeLogSQL() throws JDBCException {
		CreateTableStatement ret = (CreateTableStatement) super.getCreateChangeLogSQL();
		ret.setSchemaName("dbo");
		return ret;
	}

	@Override
	public void checkDatabaseChangeLogLockTable() throws JDBCException {
		// TODO Auto-generated method stub
		super.checkDatabaseChangeLogLockTable();
	}

	@Override
	public void checkDatabaseChangeLogTable() throws JDBCException {
		// TODO Auto-generated method stub
		super.checkDatabaseChangeLogTable();
	}

	@Override
	public String getLiquibaseSchemaName() {
		return "dbo";
	}

	@Override
	public boolean isPeculiarLiquibaseSchema() {
		return true;
	}

}
