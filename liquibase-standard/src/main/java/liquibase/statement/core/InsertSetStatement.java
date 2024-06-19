package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.LinkedList;
import java.util.List;

public class InsertSetStatement extends AbstractSqlStatement {
    private final LinkedList<InsertStatement> inserts = new LinkedList<>();
    private final int batchSize;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public InsertSetStatement(String catalogName, String schemaName, String tableName) {
        this(catalogName, schemaName, tableName, 50);
    }

    public InsertSetStatement(String catalogName, String schemaName, String tableName,int batchSize) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.batchSize = batchSize;
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public int getBatchThreshold() {
        return batchSize;
    }

    public InsertSetStatement addInsertStatement(InsertStatement statement) {
        inserts.add(statement);
        return this;
    }
    public InsertStatement peek() {
      return inserts.peek();
    }

    public List<InsertStatement> getStatements() {
        return inserts;
    }
    public InsertStatement[] getStatementsArray() {
        return inserts.toArray(new InsertStatement[0]);
    }
}
