package liquibase.statement.core

import liquibase.action.Action
import liquibase.action.core.UnparsedSql
import liquibase.statement.AbstractStatementTest

class RawActionStatementTest extends AbstractStatementTest {

    def "constructor with no arguments yields null getActions"() {
        when:
        def statement = new RawActionStatement()
        then:
        statement.getActions() == null
    }

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "actions") {
            Action[] actions = new Action[1]
            actions[0] = new UnparsedSql("TEST SQL")
            return actions;
        }
        return super.getTestPropertyValue(propertyName)
    }
}
