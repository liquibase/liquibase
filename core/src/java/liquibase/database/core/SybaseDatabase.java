package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DataType;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

public class SybaseDatabase extends AbstractDatabase {


    protected Set<String> systemTablesAndViews = new HashSet<String>();
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);
    private static final DataType DATE_TYPE = new DataType("SMALLDATETIME", false);
    private static final DataType BOOLEAN_TYPE = new DataType("BIT", false);
    private static final DataType CURRENCY_TYPE = new DataType("MONEY", false);
    private static final DataType UUID_TYPE = new DataType("UNIQUEIDENTIFIER", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType BLOB_TYPE = new DataType("IMAGE", true);


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
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
        return null;
    }

    @Override
    public String getDefaultCatalogName() throws DatabaseException {
        return getConnection().getCatalog();
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
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return getDefaultCatalogName();
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            return "dbo";
        }
        return requestedSchema;
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
    public boolean supportsRestrictForeignKeys() {
        return false;
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

        systemTablesAndViews.add("syssegments");
        systemTablesAndViews.add("sysconstraints");
        systemTablesAndViews.add("sysquerymetrics");
    }

    public String getTypeName() {
        return "sybase";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:jtds:sybase")) {
            return "net.sourceforge.jtds.jdbc.Driver";
        }
        return null;
    }

    /**
     * Sybase does not support DDL and meta data in transactions properly,
     * as such we turn off the commit and turn on auto commit.
     */
    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    /**
     * Drops all objects owned by the connected user.
     * <p/>
     * The Sybase functionality overrides this and does not remove the Foreign Keys.
     * <p/>
     * Unfortunately it appears to be a problem with the Drivers, see the JTDS driver page.
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1471425&group_id=33291&atid=407762
     */
//    public void dropDatabaseObjects(String schema) throws DatabaseException {
//        DatabaseConnection conn = getConnection();
//        try {
//            //dropForeignKeys(conn);
//            dropViews(schema, conn);
//            dropTables(schema, conn);
//
//            if (this.supportsSequences()) {
//                dropSequences(schema, conn);
//            }
//
//            changeLogTableExists = false;
//        } finally {
//            try {
//                conn.commit();
//            } catch (SQLException e) {
//                ;
//            }
//        }
//    }
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        String dbProductName = conn.getDatabaseProductName();
        return
                "Sybase SQL Server".equals(dbProductName) ||
                        "sql server".equals(dbProductName);
    }

    public boolean supportsTablespaces() {
        return true;
    }

}
