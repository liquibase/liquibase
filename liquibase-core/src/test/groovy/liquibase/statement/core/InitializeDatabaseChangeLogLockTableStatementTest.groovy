package liquibase.statement.core

import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

import java.lang.reflect.Field

class InitializeDatabaseChangeLogLockTableStatementTest extends AbstractStatementTest {

    @Override
    protected List<Field> getAllDeclaredFields() {
        return [null]
    }
}
