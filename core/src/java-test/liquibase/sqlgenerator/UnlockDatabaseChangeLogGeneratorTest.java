package liquibase.sqlgenerator;

import liquibase.statement.UnlockDatabaseChangeLogStatement;

public class UnlockDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<UnlockDatabaseChangeLogStatement> {

    public UnlockDatabaseChangeLogGeneratorTest() throws Exception {
        super(new UnlockDatabaseChangeLogGenerator());
    }

    protected UnlockDatabaseChangeLogStatement createSampleSqlStatement() {
        return new UnlockDatabaseChangeLogStatement();
    }
}
