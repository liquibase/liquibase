package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;

public class CreateDatabaseChangelogTableGeneratorZOSTest extends AbstractSqlGeneratorTest<CreateDatabaseChangeLogTableStatement> {

    public CreateDatabaseChangelogTableGeneratorZOSTest() throws Exception {
        super(new CreateDatabaseChangelogTableGeneratorZOS());
    }

    @Override
    protected CreateDatabaseChangeLogTableStatement createSampleSqlStatement() {
        return new CreateDatabaseChangeLogTableStatement();
    }
    
    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof Db2zDatabase;
    }
}