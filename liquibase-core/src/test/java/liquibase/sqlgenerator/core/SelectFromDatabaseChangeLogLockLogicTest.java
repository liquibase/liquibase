package liquibase.sqlgenerator.core;

import liquibase.actionlogic.core.SelectFromDatabaseChangeLogLockLogic;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;

public class SelectFromDatabaseChangeLogLockLogicTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogLockStatement> {
    public SelectFromDatabaseChangeLogLockLogicTest() throws Exception {
        super(null);// new SelectFromDatabaseChangeLogLockLogic());
    }

    @Override
    protected SelectFromDatabaseChangeLogLockStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }
}
