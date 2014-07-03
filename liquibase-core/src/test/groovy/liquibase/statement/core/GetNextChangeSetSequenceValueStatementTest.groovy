package liquibase.statement.core

import liquibase.statement.AbstractStatementTest

import java.lang.reflect.Field

class GetNextChangeSetSequenceValueStatementTest extends AbstractStatementTest {

    @Override
    protected List<Field> getAllDeclaredFields() {
        return [null]
    }
}
