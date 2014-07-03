package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class DeleteDataStatementTest extends AbstractStatementTest<DeleteDataStatement> {

    def "constructor"() {
        when:
        def statement = new DeleteDataStatement("CAT_NAME", "SCHEMA_NAME", "TABLE_NAME")
        then:
        statement.getCatalogName() == "CAT_NAME"
        statement.getSchemaName() == "SCHEMA_NAME"
        statement.getTableName() == "TABLE_NAME"
    }

    def "get and add column names"() {
        when:
        def statement = new DeleteDataStatement()
        then:
        statement.getWhereColumnNames() == []

        when:
        statement.addWhereColumnNames(null)
        then:
        statement.getWhereColumnNames() == []

        when:
        statement.addWhereColumnNames("id1")
        then:
        statement.getWhereColumnNames() == ["id1"]

        when:
        statement.addWhereColumnNames("id2", "id3")
        then:
        statement.getWhereColumnNames() == ["id1", "id2", "id3"]
    }

    def "get and add param values"() {
        when:
        def statement = new DeleteDataStatement()
        then:
        statement.getWhereParameters() == []

        when:
        statement.addWhereParameters(null)
        then:
        statement.getWhereParameters() == []

        when:
        statement.addWhereParameters("val1")
        then:
        statement.getWhereParameters() == ["val1"]

        when:
        statement.addWhereParameters(32, true)
        then:
        statement.getWhereParameters() == ["val1", 32, true]
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("whereColumnNames")
        properties.remove("whereParameters")
        return properties
    }
}
