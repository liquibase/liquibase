package liquibase.sqlgenerator.core;

import liquibase.actionlogic.core.SelectFromDatabaseChangeLogLogic;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class SelectFromDatabaseChangeLogLogicTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogStatement> {
    public SelectFromDatabaseChangeLogLogicTest() throws Exception {
        super( new SelectFromDatabaseChangeLogLogic());
    }

    @Override
    protected SelectFromDatabaseChangeLogStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogStatement("ID");
    }
}