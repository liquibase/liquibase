package liquibase.sqlgenerator.core;

import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogGenerator;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;

public class SelectFromDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogStatement> {
    public SelectFromDatabaseChangeLogGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogStatement("ID");
    }
}