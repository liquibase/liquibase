package liquibase.statement.core;

public class InsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;


    public InsertOrUpdateStatement(String catalogName, String schemaName, String tableName, String primaryKey) {
        super(catalogName, schemaName, tableName);
        this.primaryKey = primaryKey ;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
}
