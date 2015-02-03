package liquibase.sqlgenerator.core;

import liquibase.actionlogic.core.UnlockDatabaseChangeLogLogic;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;

public class UnlockDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<UnlockDatabaseChangeLogStatement> {

    public UnlockDatabaseChangeLogGeneratorTest() throws Exception {
        super(null); //new UnlockDatabaseChangeLogLogic());
    }

    @Override
    protected UnlockDatabaseChangeLogStatement createSampleSqlStatement() {
        return new UnlockDatabaseChangeLogStatement();
    }
}
