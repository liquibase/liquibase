package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

public class DropPrimaryKeyStatement extends AbstractPrimaryKeyStatement {

    public DropPrimaryKeyStatement() {
    }

    public DropPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        super(constraintName, catalogName, schemaName, tableName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[]{
                new PrimaryKey().setName(getConstraintName()).setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
