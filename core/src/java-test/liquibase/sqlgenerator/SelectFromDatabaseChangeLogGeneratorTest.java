package liquibase.sqlgenerator;

import liquibase.statement.SelectFromDatabaseChangeLogStatement;

public class SelectFromDatabaseChangeLogGeneratorTest extends AbstractSqlGeneratorTest<SelectFromDatabaseChangeLogStatement> {
    public SelectFromDatabaseChangeLogGeneratorTest() throws Exception {
        super( new SelectFromDatabaseChangeLogGenerator());
    }

    @Override
    protected SelectFromDatabaseChangeLogStatement createSampleSqlStatement() {
        return new SelectFromDatabaseChangeLogStatement("ID");
    }
}