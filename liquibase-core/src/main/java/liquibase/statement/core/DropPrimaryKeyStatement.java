package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

public class DropPrimaryKeyStatement extends AbstractStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
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

    public String getConstraintName() {
        return constraintName;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[]{
                new PrimaryKey().setName(getConstraintName()).setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
