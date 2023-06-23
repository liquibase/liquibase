package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.LinkedList;
import java.util.List;

public class InsertSetStatement extends AbstractSqlStatement {
    private final LinkedList<InsertStatement> inserts = new LinkedList<>();
    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final int batchSize;

    public InsertSetStatement(String catalogName, String schemaName, String tableName) {
        this(catalogName, schemaName, tableName, 50);
    }

    public InsertSetStatement(String catalogName, String schemaName, String tableName,int batchSize) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.batchSize = batchSize;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
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
