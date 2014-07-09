package liquibase.statement.core

import liquibase.changelog.ChangeSet
import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

class RemoveChangeSetRanStatusStatementTest extends AbstractStatementTest {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "changeSet") {
            return new ChangeSet("1", "test", false, false, "path", null, null, null)
        }
        return super.getTestPropertyValue(propertyName)
    }
}
