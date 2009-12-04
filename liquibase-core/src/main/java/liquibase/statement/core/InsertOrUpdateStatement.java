package liquibase.statement.core;

/**
 * Created by IntelliJ IDEA.
 * User: bassettt
 * Date: Dec 1, 2009
 * Time: 11:01:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsertOrUpdateStatement extends InsertStatement {
    private String primaryKey;


    public InsertOrUpdateStatement(String schemaName, String tableName, String primaryKey) {
        super(schemaName, tableName);
        this.primaryKey = primaryKey ;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }
}
