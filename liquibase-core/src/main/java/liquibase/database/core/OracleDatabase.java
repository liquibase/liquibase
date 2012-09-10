package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.statement.DatabaseFunction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";


    private Set<String> reservedWords = new HashSet<String>();

	public OracleDatabase() {
		// Setting list of Oracle's native functions
		dateFunctions.add(new DatabaseFunction("SYSDATE"));
		dateFunctions.add(new DatabaseFunction("SYSTIMESTAMP"));
        dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP"));
	}

	public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return objectName.toUpperCase();
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

            reservedWords.addAll(Arrays.asList(sqlConn.getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
            reservedWords.addAll(Arrays.asList("USER", "SESSION","RESOURCE")); //more reserved words not returned by driver
        } catch (Exception e) {
            LogFactory.getLogger().info("Could not set remarks reporting on OracleDatabase: "+e.getMessage());
            ; //cannot set it. That is OK
        }
        super.setConnection(conn);
    }

    public String getShortName() {
        return "oracle";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Oracle";
    }

    public Integer getDefaultPort() {
        return 1521;
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.length() > 27) {
            return "PK_" + tableName.toUpperCase().substring(0, 27);
        } else {
            return "PK_" + tableName.toUpperCase();
        }
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public String escapeDatabaseObject(String objectName, Class<? extends DatabaseObject> objectType) {
        // escape the object name if it contains any non-word characters
        if (objectName != null &&
                (Pattern.compile("\\W").matcher(objectName).find() || isReservedWord(objectName))) {
            return "\"" + objectName.trim().toUpperCase() + "\"";
        } else {
            return objectName;
        }
    }

    @Override
    public boolean isReservedWord(String objectName) {
        return reservedWords.contains(objectName.toUpperCase());
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    /**
     * Oracle supports catalogs in liquibase terms
     * @return
     */
    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    protected String doGetDefaultCatalogName() throws DatabaseException {
        try {
            ResultSet resultSet = ((JdbcConnection) getConnection()).prepareCall("select sys_context( 'userenv', 'current_schema' ) from dual").executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        } catch (Exception e) {
            LogFactory.getLogger().info("Error getting default schema", e);
        }
        return null;
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
        return "SYSTIMESTAMP";
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
    public boolean isSystemTable(Schema schema, String tableName) {
        if (super.isSystemTable(schema, tableName)) {
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

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        if (dataTypeName.toUpperCase().equals("BINARY_FLOAT")) {
            return 0;
        }
        if (dataTypeName.toUpperCase().equals("BINARY_DOUBLE")) {
            return 0;
        }
        return super.getDataTypeMaxParameters(dataTypeName);
    }
}
