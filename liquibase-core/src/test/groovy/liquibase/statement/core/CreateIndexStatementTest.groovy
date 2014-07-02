package liquibase.statement.core;

import liquibase.AbstractExtensibleObject;
import liquibase.statement.AbstractStatementTest;
import liquibase.statement.Statement;

public class CreateIndexStatementTest extends AbstractStatementTest<Statement> {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "columnNames") {
            return ["column_1", "column_2"] as String[]
        }
        return super.getTestPropertyValue(propertyName)
    }
}
