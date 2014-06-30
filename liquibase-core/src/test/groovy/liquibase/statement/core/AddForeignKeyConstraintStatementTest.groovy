package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class AddForeignKeyConstraintStatementTest extends AbstractStatementTest<AddForeignKeyConstraintStatement> {

    def "constructor"() {
        when:
        def obj = new AddForeignKeyConstraintStatement("CONST_NAME", "BASE_TABLE_CAT", "BASE_TABLE_SCHEMA", "BASE_TABLE_NAME", "BASE_COL_NAMES", "REF_TABLE_CAT", "REF_TABLE_SCHEMA", "REF_TABLE_NAME", "REF_COL_NAMES")

        then:
        obj.getConstraintName() == "CONST_NAME"
        obj.getBaseTableCatalogName() == "BASE_TABLE_CAT"
        obj.getBaseTableSchemaName() == "BASE_TABLE_SCHEMA"
        obj.getBaseTableName() == "BASE_TABLE_NAME"
        obj.getBaseColumnNames() == "BASE_COL_NAMES"

        obj.getReferencedTableCatalogName() == "REF_TABLE_CAT"
        obj.getReferencedTableSchemaName() == "REF_TABLE_SCHEMA"
        obj.getReferencedTableName() == "REF_TABLE_NAME"
        obj.getReferencedColumnNames() == "REF_COL_NAMES"
    }

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "initiallyDeferred") {
            return false
        }
        if (propertyName == "deferrable") {
            return false
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
