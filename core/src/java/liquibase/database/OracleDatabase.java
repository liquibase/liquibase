package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;

import java.sql.Connection;

/**
 * Encapsulates Oracle database support.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";

    public String getProductName() {
        return "Oracle";
    }

    public String getTypeName() {
        return "oracle";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    public boolean supportsSequences() {
        return true;
    }

    public String getBooleanType() {
        return "NUMBER(1)";
    }

    public String getCurrencyType() {
        return "NUMBER(15, 2)";
    }

    public String getUUIDType() {
        return "RAW(16)";
    }

    public String getClobType() {
        return "CLOB";
    }

    public String getBlobType() {
        return "BLOB";
    }

    public String getDateTimeType() {
        return "TIMESTAMP";
    }


    public String getDateType() {
        return "DATE";
    }

    public String getTimeType() {
        return "DATE";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
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

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getFalseBooleanValue() {
        return "0";
    }


    public String getSchemaName() throws JDBCException {//NOPMD
        return super.getSchemaName().toUpperCase();
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
            StringBuffer val = new StringBuffer(26);
            val.append("to_date(");
            val.append(normalLiteral);
            val.append(", 'YYYY-MM-DD HH24:MI:SS')");
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    protected SqlStatement getSelectChangeLogLockSQL() {
        return new RawSqlStatement((super.getSelectChangeLogLockSQL().getSqlStatement(this) + " for update").toUpperCase());
    }

    public SqlStatement createFindSequencesSQL() throws JDBCException {
        return new RawSqlStatement("SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + getSchemaName() + "'");
    }


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


    public boolean shouldQuoteValue(String value) {
        return super.shouldQuoteValue(value) && !value.startsWith("to_date(");
    }

    public boolean supportsTablespaces() {
        return true;
    }

    protected SqlStatement getViewDefinitionSql(String name) {
        return new RawSqlStatement("SELECT TEXT FROM USER_VIEWS WHERE upper(VIEW_NAME)='"+name.toUpperCase()+"'");
    }

    public boolean supportsAutoIncrement() {
        return false;
    }
    
}
