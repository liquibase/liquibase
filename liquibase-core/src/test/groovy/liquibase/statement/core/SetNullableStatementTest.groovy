package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SetNullableStatementTest extends AbstractStatementTest<SetNullableStatement> {

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "nullable") {
            return false
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
