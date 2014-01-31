package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RenamePrimaryKeyStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String oldConstraintName;
    private String newConstraintName;

    public RenamePrimaryKeyStatement(String catalogName, String schemaName, String tableName, String oldConstraintName, String newConstraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.oldConstraintName = oldConstraintName;
        this.newConstraintName = newConstraintName;
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

    public String getOldConstraintName() {
        return oldConstraintName;
    }

    public String getNewConstraintName() {
      return newConstraintName;
  }
}
