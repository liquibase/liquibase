package liquibase.database.core;


import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;

import java.util.HashSet;
import java.util.Set;


public class MaxDBDatabase extends AbstractDatabase {

    public static final String PRODUCT_NAME = "SAP DB";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

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

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    public String getTypeName() {
        return "maxdb";
    }

    public Integer getDefaultPort() {
        return 7210;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return PRODUCT_NAME;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sapdb")) {
            return "com.sap.dbtech.jdbc.DriverSapDB";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "TIMESTAMP";
    }

    @Override
    public boolean isSystemTable(Schema schema, String tableName) {
        schema = correctSchema(schema);
        if (super.isSystemTable(schema, tableName)) {
            return true;
        } else if ("DOMAIN".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSINFO".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSLOADER".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSDBA".equalsIgnoreCase(schema.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSystemView(Schema schema, String tableName) {
        schema = correctSchema(schema);
        if (super.isSystemView(schema, tableName)) {
            return true;
        } else if ("DOMAIN".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSINFO".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSLOADER".equalsIgnoreCase(schema.getName())) {
            return true;
        } else if ("SYSDBA".equalsIgnoreCase(schema.getName())) {
            return true;
        }
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }
}
