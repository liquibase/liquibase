package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DataType;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;

import java.lang.reflect.Method;
import java.sql.Types;
import java.text.ParseException;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";
    private static final DataType BOOLEAN_TYPE = new DataType("NUMBER(1)", false);
    private static final DataType CURRENCY_TYPE = new DataType("NUMBER(15, 2)", false);
    private static final DataType UUID_TYPE = new DataType("RAW(16)", false);
    private static final DataType CLOB_TYPE = new DataType("CLOB", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", true);
    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType BIGINT_TYPE = new DataType("NUMBER(19,0)", false);

    @Override
    public void setConnection(DatabaseConnection conn) {
        try {
            Method method = conn.getClass().getMethod("setRemarksReporting", Boolean.TYPE);
            method.setAccessible(true);
            method.invoke(conn, true);
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
        return DATE_TYPE;
    }

    @Override
    public DataType getBigIntType() {
        return BIGINT_TYPE;
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
        return "SYSDATE";
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
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
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

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
                    if (((String) defaultValue).indexOf("YYYY-MM-DD HH") > 0) {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'YYYY-MM-DD HH24:MI:SS'\\)$","");
                    } else if (((String) defaultValue).indexOf("YYYY-MM-DD") > 0) {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'YYYY-MM-DD'\\)$","");
                    } else {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'HH24:MI:SS'\\)$","");
                    }
                }
                defaultValue = ((String) defaultValue).replaceFirst("'\\s*$", "'"); //sometimes oracle adds an extra space after the trailing ' (see http://sourceforge.net/tracker/index.php?func=detail&aid=1824663&group_id=187970&atid=923443).  
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits);
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
