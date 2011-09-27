package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DB2Database extends AbstractDatabase {

    private int major;
    private int minor;

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2");
    }
    
    public boolean supportsColumnRename() {
        try {
            major = getConnection().getDatabaseMajorVersion();
            minor = getConnection().getDatabaseMinorVersion();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return major > 9 || (major == 9 && minor >=7);
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:db2")) {
            return "com.ibm.db2.jcc.DB2Driver";
        }
        return null;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public String getTypeName() {
        return "db2";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }

        return "CURRENT TIMESTAMP";
    }

    /**
     * Return an DB2 date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * hh:mm:ss
     * YYYY-MM-DDThh:mm:ss
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("DATE(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("TIME(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else if (isDateTime(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("TIMESTAMP(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    @Override
    public boolean shouldQuoteValue(String value) {
        return super.shouldQuoteValue(value)
                && !value.startsWith("\"SYSIBM\"");
    }


    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getViewDefinition(String schemaName, String name) throws DatabaseException {
        return super.getViewDefinition(schemaName, name).replaceFirst("CREATE VIEW \\w+ AS ", ""); //db2 returns "create view....as select
    }


    @Override
    public java.util.Date parseDate(String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(' ') > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateAsString);
            } else if (dateAsString.indexOf('.') > 0 && dateAsString.indexOf('-') > 0) {
                return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateAsString);

            } else {
                if (dateAsString.indexOf(':') > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else if (dateAsString.indexOf('.') > 0) {
                    return new SimpleDateFormat("HH.mm.ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            return getDefaultDatabaseSchemaName();
        } else {
            return requestedSchema.toUpperCase();
        }
    }

    @Override
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return null;
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.equals(getDatabaseChangeLogTableName())) {
            tableName = "DbChgLog".toUpperCase();
        } else if (tableName.equals(getDatabaseChangeLogLockTableName())) {
            tableName = "DbChgLogLock".toUpperCase();
        }

        String pkName = super.generatePrimaryKeyName(tableName);
        if (pkName.length() > 18) {
            pkName = pkName.substring(0, 17);
        }
        return pkName;
    }

    @Override
    public String escapeIndexName(String schemaName, String indexName) {
        // does not support the schema name for the index -
        return super.escapeIndexName(null, indexName);
    }

}
