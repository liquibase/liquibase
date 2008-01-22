/*
 * Copyright (C) 2005 Whitestein Technologies AG, Poststrasse 22, CH-6300 Zug, Switzerland.
 * All rights reserved. The use of this file in source or binary form requires a written license from Whitestein Technologies AG.
 *
 */
package liquibase.database;


import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;

import java.sql.Connection;
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

    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public String getProductName() {
        return "MaxDB";
    }

    public String getTypeName() {
        return "maxdb";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsSequences() {
        return true;
    }

    public String getBooleanType() {
        return "BOOLEAN";
    }

    public String getCurrencyType() {
        return "NUMERIC(15, 2)";
    }

    public String getUUIDType() {
        return "CHAR(36)";
    }

    public String getClobType() {
        return "LONG VARCHAR";
    }

    public String getBlobType() {
        return "LONG BYTE";
    }

    public String getDateTimeType() {
        return "TIMESTAMP";
    }

    public String getDateType() {
        return "DATE";
    }

    public String getTimeType() {
        return "TIME";
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

    public String getTrueBooleanValue() {
        return "TRUE";
    }

    public String getFalseBooleanValue() {
        return "FALSE";
    }

    public String getSchemaName() throws JDBCException {//NOPMD
        return super.getSchemaName().toUpperCase();
    }

    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return new RawSqlStatement("SELECT SEQUENCE_NAME FROM DOMAIN.SEQUENCES WHERE OWNER = '"
                + convertRequestedSchemaToSchema(schema) + "'");
    }

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

    public SqlStatement getViewDefinitionSql(String schemaName, String name) throws JDBCException {
        return new RawSqlStatement("SELECT DEFINITION FROM DOMAIN.VIEWDEFS WHERE upper(VIEWNAME)='" + name.toUpperCase()
                + "' AND OWNER='" + convertRequestedSchemaToSchema(schemaName) + "'");
    }

    public boolean supportsAutoIncrement() {
        return false;
    }

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

}
