package liquibase.statement.core;

import liquibase.statement.core.InsertStatement;
import liquibase.statement.SqlStatement;

public class InsertStatementTest extends AbstractSqStatementTest {

    @Override
    protected SqlStatement createStatementUnderTest() {
        return new InsertStatement(null, null);
    }

}
