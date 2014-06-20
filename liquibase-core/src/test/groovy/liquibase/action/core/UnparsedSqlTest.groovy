package liquibase.action.core

import liquibase.RuntimeEnvironment
import liquibase.database.OfflineConnection
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.executor.ExecuteResult
import liquibase.executor.ExecutionOptions
import liquibase.executor.QueryResult
import liquibase.executor.UpdateResult
import liquibase.sdk.database.MockDatabase
import spock.lang.Specification

class UnparsedSqlTest extends Specification {

    def "query"() {
        when:
        def unparsedSql = new UnparsedSql("some sql here", ";")
        def database = new MockDatabase()
        def connection = Mock(JdbcConnection)
        1 * connection.query("some sql here") >> new QueryResult([])
        database.setConnection(connection)

        then:
        unparsedSql.query(new ExecutionOptions(new RuntimeEnvironment(database))) != null
    }

    def "execute"() {
        when:
        def unparsedSql = new UnparsedSql("some sql here", ";")
        def database = new MockDatabase()
        def connection = Mock(JdbcConnection)
        1 * connection.execute("some sql here") >> new ExecuteResult()
        database.setConnection(connection)

        then:
        unparsedSql.execute(new ExecutionOptions(new RuntimeEnvironment(database))) != null
    }

    def "update"() {
        when:
        def unparsedSql = new UnparsedSql("some sql here", ";")
        def database = new MockDatabase()
        def connection = Mock(JdbcConnection)
        1 * connection.update("some sql here") >> new UpdateResult(1)
        database.setConnection(connection)

        then:
        unparsedSql.update(new ExecutionOptions(new RuntimeEnvironment(database))) != null
    }

    def "cannot call with non-JdbcConnection"() {
        when:
        def unparsedSql = new UnparsedSql("some sql here", ";")
        def database = new MockDatabase()
        database.setConnection(new OfflineConnection("offline:mock"))

        and:
        unparsedSql.update(new ExecutionOptions(new RuntimeEnvironment(database)))
        then:
        def e = thrown(DatabaseException)
        e.message == "Cannot execute SQL against a liquibase.database.OfflineConnection connection"

        when:
        unparsedSql.query(new ExecutionOptions(new RuntimeEnvironment(database)))
        then:
        e = thrown(DatabaseException)
        e.message == "Cannot execute SQL against a liquibase.database.OfflineConnection connection"

        when:
        unparsedSql.execute(new ExecutionOptions(new RuntimeEnvironment(database)))
        then:
        e = thrown(DatabaseException)
        e.message == "Cannot execute SQL against a liquibase.database.OfflineConnection connection"
    }

    def "describe returns sql + delimiter"() {
        when:
        def unparsedSql = new UnparsedSql(sql, delimiter)

        then:
        unparsedSql.describe() == expected

        where:
        sql             | delimiter | expected
        "some sql here" | null      | "some sql here"
        "some sql here" | ";"      | "some sql here;"
        "some sql here" | "\nGO\n"      | "some sql here\nGO\n"
    }
}
