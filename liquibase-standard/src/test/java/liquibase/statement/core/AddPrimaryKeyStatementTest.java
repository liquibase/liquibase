package liquibase.statement.core;

import liquibase.change.ColumnConfig;

public class AddPrimaryKeyStatementTest extends AbstractSqStatementTest<AddPrimaryKeyStatement> {

    @Override
    protected AddPrimaryKeyStatement createStatementUnderTest() {
        return new AddPrimaryKeyStatement(null, null, null, (ColumnConfig[]) null, null);
    }

   
}
