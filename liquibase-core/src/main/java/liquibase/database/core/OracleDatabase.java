package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.statement.DatabaseFunction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";

	public OracleDatabase() {
		// Setting list of Oracle's native functions
		databaseFunctions.add(new DatabaseFunction("SYSDATE"));
		databaseFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
	}

	public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public void setConnection(DatabaseConnection conn) {
        try {
        	Method wrappedConn = conn.getClass().getMethod("getWrappedConnection");
        	wrappedConn.setAccessible(true);
        	Connection sqlConn = (Connection)wrappedConn.invoke(conn);
            Method method = sqlConn.getClass().getMethod("setRemarksReporting", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(sqlConn, true);
        } catch (Exception e) {
            LogFactory.getLogger().info("Could not set remarks reporting on OracleDatabase: "+e.getMessage());
            ; //cannot set it. That is OK
        }
        super.setConnection(conn);
    }

    public String getTypeName() {
        return "oracle";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:oracle")) {
            return "oracle.jdbc.OracleDriver";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        return "CURRENT_TIMESTAMP";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
    }

    @Override
    public String escapeIndexName(String schemaName, String indexName) {
        String escapedIndexName = indexName;
        if (schemaName != null)
        {
            escapedIndexName = schemaName + "." + escapedIndexName;
        }
        return escapedIndexName;
    }

    /**
     * Return an Oracle date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * YYYY-MM-DDThh:mm:ss
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD')");
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'HH24:MI:SS')");
            return val.toString();
        } else if (isDateTime(isoDate)) {
            normalLiteral = normalLiteral.substring(0, normalLiteral.lastIndexOf('.'))+"'";

            StringBuffer val = new StringBuffer(26);
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD HH24:MI:SS')");
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    @Override
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        if (super.isSystemTable(catalogName, schemaName, tableName)) {
            return true;
        } else if (tableName.startsWith("BIN$")) { //oracle deleted table
            return true;
        } else if (tableName.startsWith("AQ$")) { //oracle AQ tables
            return true;
        } else if (tableName.startsWith("DR$")) { //oracle index tables
            return true;
        } else if (tableName.startsWith("SYS_IOT_OVER")) { //oracle system table
            return true;
        }
        return false;
    }
    
    @Override
    public boolean shouldQuoteValue(String value) {
        return super.shouldQuoteValue(value)
            && !value.startsWith("to_date(")
            && !value.equalsIgnoreCase(getCurrentDateTimeFunction());
    }

    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }


//    public Set<UniqueConstraint> findUniqueConstraints(String schema) throws DatabaseException {
//        Set<UniqueConstraint> returnSet = new HashSet<UniqueConstraint>();
//
//        List<Map> maps = new Executor(this).queryForList(new RawSqlStatement("SELECT UC.CONSTRAINT_NAME, UCC.TABLE_NAME, UCC.COLUMN_NAME FROM USER_CONSTRAINTS UC, USER_CONS_COLUMNS UCC WHERE UC.CONSTRAINT_NAME=UCC.CONSTRAINT_NAME AND CONSTRAINT_TYPE='U' ORDER BY UC.CONSTRAINT_NAME"));
//
//        UniqueConstraint constraint = null;
//        for (Map map : maps) {
//            if (constraint == null || !constraint.getName().equals(constraint.getName())) {
//                returnSet.add(constraint);
//                Table table = new Table((String) map.get("TABLE_NAME"));
//                constraint = new UniqueConstraint(map.get("CONSTRAINT_NAME").toString(), table);
//            }
//        }
//        if (constraint != null) {
//            returnSet.add(constraint);
//        }
//
//        return returnSet;
//    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }
}
