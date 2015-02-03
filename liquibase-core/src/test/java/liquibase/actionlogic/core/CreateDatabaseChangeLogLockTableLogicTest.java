package liquibase.actionlogic.core;

import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

public class CreateDatabaseChangeLogLockTableLogicTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableLogicTest() throws Exception {
        super(null);//new CreateDatabaseChangeLogLockTableLogic());
    }

    @Override
    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
