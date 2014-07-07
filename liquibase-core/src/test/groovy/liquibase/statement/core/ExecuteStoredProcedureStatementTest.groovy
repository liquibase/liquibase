package liquibase.statement.core;

import liquibase.statement.AbstractStatementTest;

public class ExecuteStoredProcedureStatementTest extends AbstractStatementTest<ExecuteStoredProcedureStatement> {

    def "Adding parameters"() {
        when:
        def statement = new ExecuteStoredProcedureStatement()
        then:
        statement.getParameters().size() == 0

        when:
        statement.addParameter("param1")
        then:
        statement.getParameters() == ["param1"]

        when:
        statement.addParameter("param2")
        then:
        statement.getParameters() == ["param1", "param2"]
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("parameters")
        return properties
    }
}
