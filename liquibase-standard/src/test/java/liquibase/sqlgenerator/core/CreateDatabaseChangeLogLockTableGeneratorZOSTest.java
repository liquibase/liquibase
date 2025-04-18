package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

public class CreateDatabaseChangeLogLockTableGeneratorZOSTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogLockTableStatement> {

    public CreateDatabaseChangeLogLockTableGeneratorZOSTest() throws Exception {
        super(new CreateDatabaseChangeLogLockTableGeneratorZOS());
    }

    @Override
    protected CreateDatabaseChangeLogLockTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogLockTableStatement();
    }
    
    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof Db2zDatabase;
    }
}