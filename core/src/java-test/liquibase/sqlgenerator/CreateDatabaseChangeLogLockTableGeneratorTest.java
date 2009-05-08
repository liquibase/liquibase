package liquibase.sqlgenerator;

import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;

public class CreateDatabaseChangeLogLockTableGeneratorTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableGeneratorTest() throws Exception {
        super(new CreateDatabaseChangeLogLockTableGenerator());
    }

    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
}
