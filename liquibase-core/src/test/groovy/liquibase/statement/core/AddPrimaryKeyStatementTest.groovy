package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AddPrimaryKeyStatementTest extends AbstractStatementTest<AddPrimaryKeyStatement> {

    def "constructor"() {
        when:
        def obj = new AddPrimaryKeyStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME", "COLUMN1, COLUMN2", "CONST_NAME")

        then:
        obj.getCatalogName() == "CAT_NAME"
        obj.getSchemaName() == "SCHEMA_NAME"
        obj.getTableName() == "TABLE_NAME"
        obj.getColumnNames() == "COLUMN1, COLUMN2"
        obj.getConstraintName() == "CONST_NAME"
    }

}
