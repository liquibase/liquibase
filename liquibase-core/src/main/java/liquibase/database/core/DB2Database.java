package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DB2Database extends AbstractDatabase {

    public DB2Database() {
        super();
        super.sequenceNextValueFunction = "nextval for %s";
    }

    private String defaultSchemaName;

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2");
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

    public Integer getDefaultPort() {
        return 446;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "DB2";
    }

    public String getShortName() {
        return "db2";
    }

    @Override
    public String getDefaultCatalogName() {
        if (defaultSchemaName == null) {
            if (getConnection() == null) {
                return null;
            }
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = ((JdbcConnection) getConnection()).createStatement();
                rs = stmt.executeQuery("select current schema from sysibm.sysdummy1");
                if (rs.next()) {
                    String result = rs.getString(1);
                    if (result != null) {
                        this.defaultSchemaName = result;
                    } else {
                        this.defaultSchemaName = super.getDefaultSchemaName();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Could not determine current schema", e);
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(stmt);
            }

        }
        return defaultSchemaName;
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return objectName.toUpperCase();
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



    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getViewDefinition(Schema schema, String name) throws DatabaseException {
        return super.getViewDefinition(schema, name).replaceFirst("CREATE VIEW \\w+ AS ", ""); //db2 returns "create view....as select
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
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        // does not support the schema name for the index -
        return super.escapeIndexName(null, null, indexName);
    }

    @Override
    public String getAssumedCatalogName(String catalogName, String schemaName) {
        return schemaName;
    }
}
