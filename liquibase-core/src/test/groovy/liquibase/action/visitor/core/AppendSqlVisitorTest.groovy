package liquibase.action.visitor.core

import liquibase.RuntimeEnvironment
import liquibase.executor.ExecutionOptions
import liquibase.sdk.database.MockDatabase
import liquibase.sql.visitor.StandardActionVisitorTest
import spock.lang.Specification
import spock.lang.Unroll

class AppendSqlVisitorTest extends StandardActionVisitorTest {

    @Unroll("#featureName '#value'")
    def "modifySql with value"() {
        when:
        def originalSql = "Some starting sql"
        def visitor = new AppendSqlVisitor()
        visitor.setValue(value)
        def finalSql = visitor.modifySql(originalSql, new ExecutionOptions(new RuntimeEnvironment(new MockDatabase())))

        then:
        finalSql == expected

        where:
        value   | expected
        null    | "Some starting sql"
        ""      | "Some starting sql"
        " here" | "Some starting sql here"
    }
}
