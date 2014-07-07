package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest
import spock.lang.Unroll;

public class SelectFromDatabaseChangeLogStatementTest extends AbstractStatementTest<SelectFromDatabaseChangeLogStatement> {

    @Unroll
    def "setting orderBy and columnsToSelect to null or empty columns yields null collection"() {
        when:
        def statement = new SelectFromDatabaseChangeLogStatement()
        statement.setColumnsToSelect(value)
        statement.setOrderBy(value)

        then:
        statement.getColumnsToSelect() == null
        statement.getOrderBy() == null

        where:
        value << [null, new String[0]]
    }

    @Override
    protected Object getTestPropertyValue(String propertyName) {
        if (propertyName == "whereClause") {
            return new SelectFromDatabaseChangeLogStatement.ByTag("TEST")
        } else if (propertyName == "orderBy" || "columnsToSelect") {
            return ["ID", "DATE"] as String[]
        }
        return super.getTestPropertyValue(propertyName)
    }
}