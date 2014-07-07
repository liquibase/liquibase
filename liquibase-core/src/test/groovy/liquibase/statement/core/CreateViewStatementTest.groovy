package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class CreateViewStatementTest extends AbstractStatementTest {

    def "constructor"() {
        when:
        def statement = new CreateViewStatement("CAT_NAME", "SCHEMA_NAME", "VIEW_NAME", "SELECT QUERY", true)

        then:
        statement.catalogName == "CAT_NAME"
        statement.schemaName == "SCHEMA_NAME"
        statement.viewName == "VIEW_NAME"
        statement.selectQuery == "SELECT QUERY"
        assert statement.isReplaceIfExists()
    }

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "replaceIfExists") {
            return false;
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}