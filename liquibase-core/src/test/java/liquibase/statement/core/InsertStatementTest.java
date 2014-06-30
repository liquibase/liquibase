package liquibase.statement.core;

import liquibase.AbstractExtensibleObject;
import liquibase.statement.AbstractStatementTest;
import liquibase.statement.Statement;

public class InsertStatementTest extends AbstractStatementTest {

    @Override
    protected AbstractExtensibleObject createObject() {
        return new InsertStatement(null, null, null);
    }

}
