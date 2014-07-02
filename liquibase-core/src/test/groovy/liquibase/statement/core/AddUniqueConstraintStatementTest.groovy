package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AddUniqueConstraintStatementTest extends AbstractStatementTest<AddUniqueConstraintStatement> {

    def "constructor"() {
        when:
        def obj = new AddUniqueConstraintStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN1, COLUMN2", "CONST_NAME")

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnNames() == "COLUMN1, COLUMN2"
        obj.getConstraintName() == "CONST_NAME"
    }

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "initiallyDeferred" || propertyName == "deferrable" || propertyName == "disabled") {
            return false
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
