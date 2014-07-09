package liquibase.statement.core

import liquibase.change.ColumnConfig
import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

class CopyDataStatementTest extends AbstractStatementTest {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "sourceColumns") {
            List test = new ArrayList();
            test.add(new ColumnConfig().setName("id"))
            test.add(new ColumnConfig().setName("name"))

            return test;
        }
        return super.getTestPropertyValue(propertyName)
    }
}
