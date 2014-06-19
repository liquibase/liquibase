package liquibase.action.visitor.core

import liquibase.RuntimeEnvironment
import liquibase.executor.ExecutionOptions
import liquibase.sdk.database.MockDatabase
import liquibase.sql.visitor.StandardActionVisitorTest
import spock.lang.Specification
import spock.lang.Unroll

class ReplaceSqlVisitorTest extends StandardActionVisitorTest {

    @Unroll("#featureName '#replace' -> '#with'")
    def "modifySql with value"() {
        when:
        def originalSql = "Some starting sql"
        def visitor = new ReplaceSqlVisitor()
        visitor.setReplace(replace)
        visitor.setWith(with)
        def finalSql = visitor.modifySql(originalSql, new ExecutionOptions(new RuntimeEnvironment(new MockDatabase())))

        then:
        finalSql == expected

        where:
        replace    | with        | expected
        null       | null        | "Some starting sql"
        "starting" | null        | "Some starting sql"
        null       | "changed"   | "Some starting sql"
        "sql"      | "statement" | "Some starting statement"
    }

}
