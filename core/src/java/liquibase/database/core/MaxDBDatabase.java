package liquibase.database.core;


import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.MaxDBDatabaseSnapshot;
import liquibase.database.AbstractDatabase;
import liquibase.database.DataType;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;


public class MaxDBDatabase extends AbstractDatabase {

    public static final String PRODUCT_NAME = "SAP DB";
    protected Set<String> systemTablesAndViews = new HashSet<String>();
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType CURRENCY_TYPE = new DataType("NUMERIC(15, 2)", false);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("LONG VARCHAR", false);
    private static final DataType BLOB_TYPE = new DataType("LONG BYTE", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);
    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType TIME_TYPE = new DataType("TIME", false);

    public MaxDBDatabase() {
        super();
        systemTablesAndViews.add("---");

        systemTablesAndViews.add("ACTIVECONFIGURATION");
        systemTablesAndViews.add("BACKUPTHREADS");
        systemTablesAndViews.add("CACHESTATISTICS");
        systemTablesAndViews.add("CATALOGCACHESTATISTICS");
        systemTablesAndViews.add("COMMANDCACHESTATISTICS");
        systemTablesAndViews.add("COMMANDCACHESTATISTICSRESET");
        systemTablesAndViews.add("COMMANDSTATISTICS");
        systemTablesAndViews.add("COMMANDSTATISTICSRESET");
        systemTablesAndViews.add("CONSISTENTVIEWS");
        systemTablesAndViews.add("DATACACHE");
        systemTablesAndViews.add("DATASTATISTICS");
        systemTablesAndViews.add("DATASTATISTICSRESET");
        systemTablesAndViews.add("DATAVOLUMES");
        systemTablesAndViews.add("DATASTATISTICSRESET");
        systemTablesAndViews.add("FILEDIRECTORIES");
        systemTablesAndViews.add("FILES");
        systemTablesAndViews.add("HOTSTANDBYCOMPONENT");
        systemTablesAndViews.add("HOTSTANDBYGROUP");
        systemTablesAndViews.add("INSTANCE");
        systemTablesAndViews.add("IOBUFFERCACHES");
        systemTablesAndViews.add("IOTHREADSTATISTICS");
        systemTablesAndViews.add("IOTHREADSTATISTICSRESET");
        systemTablesAndViews.add("INDEXSIZE");
        systemTablesAndViews.add("INDEXSTORAGEDETAILS");
        systemTablesAndViews.add("LOGINFORMATION");
        systemTablesAndViews.add("LOGQUEUESTATISTICS");
        systemTablesAndViews.add("LOGQUEUESTATISTICSRESET");
        systemTablesAndViews.add("LOGSTATISTICS");
        systemTablesAndViews.add("LOGSTATISTICSRESET");
        systemTablesAndViews.add("LOGVOLUMES");
        systemTablesAndViews.add("MACHINECONFIGURATION");
        systemTablesAndViews.add("MACHINEUTILIZATION");
        systemTablesAndViews.add("MEMORYALLOCATORSTATISTICS");
        systemTablesAndViews.add("OPTIMIZERINFORMATION");
        systemTablesAndViews.add("READERWRITERLOCKINFORMATION");
        systemTablesAndViews.add("READERWRITERLOCKSTATISTICS");
        systemTablesAndViews.add("READERWRITERLOCKSTATISTICSRESET");
        systemTablesAndViews.add("READERWRITERLOCKWAITINGTASKS");
        systemTablesAndViews.add("REGIONINFORMATION");
        systemTablesAndViews.add("REGIONSTATISTICS");
        systemTablesAndViews.add("REGIONSTATISTICSRESET");
        systemTablesAndViews.add("RESTARTINFORMATION");
        systemTablesAndViews.add("SCHEMASIZE");
        systemTablesAndViews.add("SERVERTASKS");
        systemTablesAndViews.add("SESSIONS");
        systemTablesAndViews.add("SNAPSHOTS");
        systemTablesAndViews.add("SPINLOCKPOOLSTATISTICS");
        systemTablesAndViews.add("SPINLOCKPOOLSTATISTICSRESET");
        systemTablesAndViews.add("SPINLOCKSTATISTICS");
        systemTablesAndViews.add("SPINLOCKSTATISTICSRESET");
        systemTablesAndViews.add("TABLESIZE");
        systemTablesAndViews.add("TABLESTORAGEDETAILS");
        systemTablesAndViews.add("TASKGROUPSTATISTICS");
        systemTablesAndViews.add("TASKGROUPSTATISTICSRESET");
        systemTablesAndViews.add("TASKLOADBALANCINGINFORMATION");
        systemTablesAndViews.add("TASKLOADBALANCINGTASKGROUPSTATES");
        systemTablesAndViews.add("TASKLOADBALANCINGTASKMOVES");
        systemTablesAndViews.add("TRANSACTIONHISTORY");
        systemTablesAndViews.add("TRANSACTIONS");
        systemTablesAndViews.add("UNLOADEDSTATEMENTS");
        systemTablesAndViews.add("VERSION");
    }

    @Override
    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public String getTypeName() {
        return "maxdb";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
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

    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    @Override
    public DataType getDateType() {
        return DATE_TYPE;
    }

    @Override
    public DataType getTimeType() {
        return TIME_TYPE;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sapdb")) {
            return "com.sap.dbtech.jdbc.DriverSapDB";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        return "TIMESTAMP";
    }

    @Override
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    @Override
    public String getFalseBooleanValue() {
        return "FALSE";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws JDBCException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
    }

    @Override
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        if (super.isSystemTable(catalogName, schemaName, tableName)) {
            return true;
        } else if ("DOMAIN".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSINFO".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSLOADER".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSDBA".equalsIgnoreCase(schemaName)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSystemView(String catalogName, String schemaName, String tableName) {
        if (super.isSystemView(catalogName, schemaName, tableName)) {
            return true;
        } else if ("DOMAIN".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSINFO".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSLOADER".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("SYSDBA".equalsIgnoreCase(schemaName)) {
            return true;
        }
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public SqlStatement getViewDefinitionSql(String schemaName, String name) throws JDBCException {
        return new RawSqlStatement("SELECT DEFINITION FROM DOMAIN.VIEWDEFS WHERE upper(VIEWNAME)='" + name.toUpperCase()
                + "' AND OWNER='" + convertRequestedSchemaToSchema(schemaName) + "'");
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    @Override
    public String convertJavaObjectToString(Object value) {
        if (value instanceof Boolean) {
            if (((Boolean) value)) {
                return this.getTrueBooleanValue();
            } else {
                return this.getFalseBooleanValue();
            }
        } else {
            return super.convertJavaObjectToString(value);
        }
    }

    @Override
    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new MaxDBDatabaseSnapshot(this, statusListeners, schema);
    }
}
