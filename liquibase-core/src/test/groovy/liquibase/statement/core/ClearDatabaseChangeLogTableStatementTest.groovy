package liquibase.statement.core

import liquibase.statement.AbstractStatementTest

class ClearDatabaseChangeLogTableStatementTest extends AbstractStatementTest {

    def "constructor"() {
        when:
        def obj = new ClearDatabaseChangeLogTableStatement("CAT_NAME", "SCHEMA_NAME")

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
    }

}
