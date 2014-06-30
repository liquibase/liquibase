package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AddDefaultValueStatementTest extends AbstractStatementTest<AddDefaultValueStatement> {

    def "constructor"() {
        when:
        def obj = new AddDefaultValueStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN_NAME", "DATA TYPE", 165)

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnName() == "COLUMN_NAME"
        obj.getColumnDataType() == "DATA TYPE"
        obj.getDefaultValue() == 165
    }

}