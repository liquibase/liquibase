package liquibase.database.core;

import java.math.BigInteger;
import java.sql.SQLException;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    public String getTypeName() {
        return "mysql";
    }


//todo: handle    @Override
//    public String getConnectionUsername() throws DatabaseException {
//        return super.getConnection().getConnectionUserName().replaceAll("\\@.*", "");
//    }

    @Override
    public String correctPrimaryKeyName(String pkName)  {
        if (pkName.equals("PRIMARY")) {
            return null;
        } else {
            return pkName;
        }
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "MySQL";
    }

    public Integer getDefaultPort() {
        return 3306;
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
    }


    @Override
    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "NOW()";
    }

    @Override
    public String getLineComment() {
        return "-- ";
    }

    @Override
    protected String getAutoIncrementClause() {
    	return "AUTO_INCREMENT";
    }    

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
    	// incrementBy not supported
    	return false;
    }
   
    @Override
    protected String getAutoIncrementOpening() {
    	return "";
    }
    
    @Override
    protected String getAutoIncrementClosing() {
    	return "";
    }
    
    @Override
    protected String getAutoIncrementStartWithClause() {
    	return "=%d";
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
    public String getDefaultSchemaName() {
        return null;
    }

    @Override
    public String escapeDatabaseObject(String objectName) {
        return "`"+objectName+"`";
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return escapeDatabaseObject(indexName);
    }


    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return getPrefix(catalogName, schemaName)+tableName;
    }

    private String getPrefix(String catalogName, String schemaName) {
        String prefix = "";
        if (catalogName != null) {
            prefix = catalogName+".";
        } else if (schemaName != null) {
            prefix = schemaName + ".";
        }
        return prefix;
    }

    @Override
    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return getPrefix(catalogName, schemaName)+viewName;
    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return true;
    }

    @Override
    public boolean disableForeignKeyChecks() throws DatabaseException {
        boolean enabled = ExecutorService.getInstance().getExecutor(this).queryForInt(new RawSqlStatement("SELECT @@FOREIGN_KEY_CHECKS")) == 1;
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=0"));
        return enabled;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=1"));
    }
}
