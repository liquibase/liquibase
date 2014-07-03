package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;

public class DropForeignKeyConstraintStatement extends AbstractForeignKeyStatement {

    public DropForeignKeyConstraintStatement() {
    }

    public DropForeignKeyConstraintStatement(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName) {
        super(constraintName, baseTableCatalogName, baseTableSchemaName, baseTableName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new ForeignKey().setName(getConstraintName()).setForeignKeyTable((Table) new Table().setName(getBaseTableName()).setSchema(getBaseTableCatalogName(), getBaseTableSchemaName()))
        };
    }
}
