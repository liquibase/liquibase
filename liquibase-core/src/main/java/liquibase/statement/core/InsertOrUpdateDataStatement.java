package liquibase.statement.core;

/**
 * Inserts data into an existing table if it does not exist. Updates existing data if it already does.
 */
public class InsertOrUpdateDataStatement extends InsertDataStatement {

    public static final String PRIMARY_KEY = "primaryKey";


    public InsertOrUpdateDataStatement() {
    }

    public InsertOrUpdateDataStatement(String catalogName, String schemaName, String tableName, String primaryKey) {
        super(catalogName, schemaName, tableName);
        setPrimaryKey(primaryKey);
    }

    /**
     * Returns the primary key column name used to identify existing data.
     */
    public String getPrimaryKey() {
        return getAttribute(PRIMARY_KEY, String.class);
    }

    InsertOrUpdateDataStatement setPrimaryKey(String primaryKey) {
        return (InsertOrUpdateDataStatement) setAttribute(PRIMARY_KEY, primaryKey);
    }
}
