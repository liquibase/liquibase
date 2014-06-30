package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropForeignKeyConstraintStatementTest extends AbstractStatementTest<DropForeignKeyConstraintStatement> {

    @Override
    protected DropForeignKeyConstraintStatement createObject() {
        return new DropForeignKeyConstraintStatement(null, null, null, null);
    }
}
