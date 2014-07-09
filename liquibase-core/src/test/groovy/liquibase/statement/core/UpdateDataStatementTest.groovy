package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest
import org.hamcrest.Matchers

import static spock.util.matcher.HamcrestSupport.that;

public class UpdateDataStatementTest extends AbstractStatementTest {

    def "new column values logic"() {
        when:
        def statement = new UpdateDataStatement()

        then:
        statement.getColumnNames().size() == 0
        statement.getNewColumnValue("id") == null

        when:
        statement.addNewColumnValue("id", 121)
        then:
        that statement.getColumnNames(), Matchers.contains(["id"] as Object[])
        statement.getNewColumnValue("id") == 121

        when:
        statement.addNewColumnValue("address", "333 4th St")
        then:
        that statement.getColumnNames(), Matchers.contains(["address", "id"] as Object[])
        statement.getNewColumnValue("id") == 121
        statement.getNewColumnValue("address") == "333 4th St"

        when:
        statement.removeNewColumnValue("id")
        then:
        that statement.getColumnNames(), Matchers.contains(["address"] as Object[])
        statement.getNewColumnValue("id") == null
        statement.getNewColumnValue("address") == "333 4th St"
    }

    def "get and add where parameters"() {
        when:
        def statement = new UpdateDataStatement()
        then:
        statement.getWhereParameters() == []

        when:
        statement.addWhereParameters()
        then:
        statement.getWhereParameters() == []

        when:
        statement.addWhereParameters("id")
        then:
        statement.getWhereParameters() == ["id"]

        when:
        statement.addWhereParameters("address", "name")
        then:
        statement.getWhereParameters() == ["id", "address", "name"]
    }

    def "get and add where column names"() {
        when:
        def statement = new UpdateDataStatement()
        then:
        statement.getWhereColumnNames() == []

        when:
        statement.addWhereColumnNames()
        then:
        statement.getWhereColumnNames() == []

        when:
        statement.addWhereColumnNames("id")
        then:
        statement.getWhereColumnNames() == ["id"]

        when:
        statement.addWhereColumnNames("address", "name")
        then:
        statement.getWhereColumnNames() == ["id", "address", "name"]
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("whereColumnNames")
        properties.remove("whereParameters")
        properties.remove("columnNames")
        return properties
    }

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "needsPreparedStatement") {
            return false
        }
        return super.getDefaultPropertyValue(propertyName)
    }
}
