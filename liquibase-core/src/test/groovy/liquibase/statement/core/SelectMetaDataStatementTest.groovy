package liquibase.statement.core

import liquibase.statement.AbstractStatementTest
import liquibase.structure.core.Table

class SelectMetaDataStatementTest extends AbstractStatementTest {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "example") {
            return new Table().setName("TEST")
        }
        return super.getTestPropertyValue(propertyName)
    }
}
