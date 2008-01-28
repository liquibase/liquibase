package liquibase.database;

import liquibase.exception.JDBCException;

import java.sql.Connection;
import java.text.ParseException;

public class CacheDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "cache";

    public String getBlobType() {
        return "LONGVARBINARY";
    }

    public String getBooleanType() {
        return "INTEGER";
    }

    public String getClobType() {
        return "LONGVARCHAR";
    }

    public String getCurrencyType() {
        return "MONEY";
    }

    public String getDateTimeType() {
        return "DATETIME";
    }

    public String getUUIDType() {
        return "CHAR(36)";
    }

    public String getCurrentDateTimeFunction() {
        return "SYSDATE";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:Cache")) {
            return "com.intersys.jdbc.CacheDriver";
        }
        return null;
    }

    public String getProductName() {
        return "Cache";
    }

    public String getTypeName() {
        return "cache";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn)
            throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public String getLineComment() {
        return "--";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws JDBCException {
        return "";
    }

    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public boolean supportsAutoIncrement() {
        return false;
    }

    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                String stringDefaultValue = (String) defaultValue;
                if (stringDefaultValue.charAt(0) == '"' && stringDefaultValue.charAt(stringDefaultValue.length() - 1) == '"') {
                    defaultValue = stringDefaultValue.substring(1, stringDefaultValue.length() - 1);
                } else if (stringDefaultValue.startsWith("$")) {
                    defaultValue = "OBJECTSCRIPT '" + defaultValue + "'";
                }
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits);

    }

    public String getViewDefinition(String schemaName, String viewName) throws JDBCException {
        return null;
    }
}
