package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DropTableStatementTest extends AbstractStatementTest<DropTableStatement> {

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "cascadeConstraints") {
            return false
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
