package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class SelectFromDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogStatement> {
    public SelectFromDatabaseChangeLogGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogStatement("ID");
    }
}