package liquibase.statement.core

import liquibase.AbstractExtensibleObject
import liquibase.statement.AbstractStatementTest

public class DropColumnStatementTest extends AbstractStatementTest {

    @Override
    protected AbstractExtensibleObject createObject() {
        return new DropColumnStatement(null, null, null, null);
    }

}