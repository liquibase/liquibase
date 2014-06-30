package liquibase.statement.core;

import liquibase.AbstractExtensibleObject;
import liquibase.statement.AbstractStatementTest;
import liquibase.statement.Statement;

public class CreateIndexStatementTest extends AbstractStatementTest<Statement> {

    @Override
    protected AbstractExtensibleObject createObject() {
        return new CreateIndexStatement(null, null, null, null, null, null);
    }

}
