package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DataType;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "MySQL";
    private static final DataType BOOLEAN_TYPE = new DataType("TINYINT(1)", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType BLOB_TYPE = new DataType("BLOB", true);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);

    public String getTypeName() {
        return "mysql";
    }


//todo: handle    @Override
//    public String getConnectionUsername() throws DatabaseException {
//        return super.getConnection().getConnectionUserName().replaceAll("\\@.*", "");
//    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
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
    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    @Override
    public String getLineComment() {
        return "--";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
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


    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
//        return super.getDefaultDatabaseSchemaName().replaceFirst("\\@.*","");
            return getConnection().getCatalog();
    }

    @Override
    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        if (requestedSchema == null) {
            return getDefaultDatabaseSchemaName();
        }
        return requestedSchema;
    }

    @Override
    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return requestedSchema;
    }

    @Override
    public String escapeDatabaseObject(String objectName) {
        return "`"+objectName+"`";
    }
}
