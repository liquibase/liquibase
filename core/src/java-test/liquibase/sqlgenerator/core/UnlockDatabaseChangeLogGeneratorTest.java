package liquibase.sqlgenerator.core;

import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.sqlgenerator.core.UnlockDatabaseChangeLogGenerator;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;

public class UnlockDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<UnlockDatabaseChangeLogStatement> {

    public UnlockDatabaseChangeLogGeneratorTest() throws Exception {
        super(new UnlockDatabaseChangeLogGenerator());
    }

    @Override
    protected UnlockDatabaseChangeLogStatement createSampleSqlStatement() {
        return new UnlockDatabaseChangeLogStatement();
    }
}
