package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;

import java.sql.Connection;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    public String getProductName() {
        return "MySQL";
    }

    public String getTypeName() {
        return "mysql";
    }


    public String getConnectionUsername() throws JDBCException {
        return super.getConnectionUsername().replaceAll("\\@.*", "");
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
    }


    public String getBooleanType() {
        return "TINYINT(1)";
    }

    public String getCurrencyType() {
        return "DECIMAL";
    }

    public String getUUIDType() {
        return "CHAR(36)";
    }

    public String getClobType() {
        return "TEXT";
    }

    public String getBlobType() {
        return "BLOB";
    }

    public String getDateTimeType() {
        return "DATETIME";
    }

    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    public String getLineComment() {
        return "==";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getConcatSql(String ... values) {
        StringBuffer returnString = new StringBuffer();
        returnString.append("CONCAT_WS(");
        for (String value : values) {
            returnString.append(value).append(", ");
        }

        return returnString.toString().replaceFirst(", $", ")");
    }

    public boolean supportsTablespaces() {
        return false;
    }


    public String getSchemaName() throws JDBCException {
        return super.getSchemaName().replaceFirst("\\@.*","");
    }

    public String getCatalogName() throws JDBCException {
        return super.getCatalogName();
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        if (requestedSchema == null) {
            return getSchemaName();
        }
        return requestedSchema;
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
        return requestedSchema;
    }

    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        return new RawSqlStatement("select view_definition from information_schema.views where upper(table_name)='" + viewName.toUpperCase() + "'  and table_schema='" + convertRequestedSchemaToSchema(schemaName) + "'");
    }
}
