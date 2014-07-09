package liquibase.statement.core

import liquibase.changelog.ChangeSet
import liquibase.statement.AbstractStatementTest

class MarkChangeSetRanStatementTest extends AbstractStatementTest {

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "execType") {
            return ChangeSet.ExecType.EXECUTED;
        } else if (propertyName == "changeSet") {
            return new ChangeSet("1", "test", false, false, null, null, null, null);
        }
        return super.getTestPropertyValue(propertyName)
    }
}
