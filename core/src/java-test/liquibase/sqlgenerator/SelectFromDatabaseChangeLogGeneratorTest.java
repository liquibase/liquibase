package liquibase.sqlgenerator;

import liquibase.statement.SelectFromDatabaseChangeLogStatement;

public class SelectFromDatabaseChangeLogGeneratorTest<T extends SelectFromDatabaseChangeLogStatement> extends AbstractSqlGeneratorTest<T> {
    public SelectFromDatabaseChangeLogGeneratorTest() throws Exception {
        super(new SelectFromDatabaseChangeLogGenerator());
    }

    @Override
    protected T createSampleSqlStatement() {
        return (T) new SelectFromDatabaseChangeLogStatement("ID");
    }
}