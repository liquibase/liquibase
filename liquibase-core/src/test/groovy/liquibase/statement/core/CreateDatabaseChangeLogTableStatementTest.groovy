package liquibase.statement.core

import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

class CreateDatabaseChangeLogTableStatementTest extends AbstractStatementTest {

    @Override
    protected List<String> getStandardProperties() {
        return ["NONE"]
    }
}
