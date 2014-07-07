package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class SelectFromDatabaseChangeLogLockStatementTest extends AbstractStatementTest<SelectFromDatabaseChangeLogLockStatement> {

    def "constructor with no arguments yields null getColumnsToSelect"() {
        when:
        def statement = new SelectFromDatabaseChangeLogLockStatement()
        then:
        statement.getColumnsToSelect() == null
    }


    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "columnsToSelect") {
            return [ "ID", "LOCKED" ] as String[]
        }
        return super.getTestPropertyValue(propertyName)
    }
}
