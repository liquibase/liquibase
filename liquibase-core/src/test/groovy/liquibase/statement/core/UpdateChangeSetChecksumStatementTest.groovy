package liquibase.statement.core

import liquibase.changelog.ChangeSet
import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

class UpdateChangeSetChecksumStatementTest extends AbstractStatementTest {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "changeSet") {
            return new ChangeSet("1", "test", false, false, "changelog.xml", null, null, null);
        }
        return super.getTestPropertyValue(propertyName)
    }
}
