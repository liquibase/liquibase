package liquibase.database.core

import liquibase.CatalogAndSchema
import liquibase.database.DatabaseConnection
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.executor.QueryResult
import liquibase.statement.core.GetViewDefinitionStatement
import spock.lang.Specification
import spock.lang.Unroll

public class SybaseDatabaseTest  extends Specification {

    def cleanup() {
        ExecutorService.getInstance().reset()
    }

	def testIsSybaseProductName() {
        when:
		def SybaseDatabase database = new SybaseDatabase()

        then:
		assert database.isSybaseProductName("Sybase SQL Server")
		assert database.isSybaseProductName("sql server")
		assert database.isSybaseProductName("ASE")
		assert database.isSybaseProductName("Adaptive Server Enterprise")
	}

    @Unroll
	def getViewDefinition() throws Exception {
        when:
        def executor = Mock(Executor)
        executor.query(_ as GetViewDefinitionStatement) >> new QueryResult(viewRows)
		SybaseDatabase database = new SybaseDatabase()
        ExecutorService.getInstance().setExecutor(database, executor)

        then:
		database.getViewDefinition(new CatalogAndSchema(null, "dbo"), "view_name") == expected

        where:
        viewRows | expected
        [] | ""
        [[view: "foo"]] | "foo"
        [[view: "foo"], [view: " bar"], [view: " bat"]] | "foo bar bat"
	}

	def testGetDatabaseVersionWhenImplemented() throws Exception {
        when:
		def connection = Mock(DatabaseConnection)
        connection.getDatabaseMajorVersion() >> 15
        connection.getDatabaseMinorVersion() >> 5
		
		def database = new SybaseDatabase()
		database.setConnection(connection)

        then:
		database.getDatabaseMajorVersion() == 15
        database.getDatabaseMinorVersion() == 5
	}
	
	def testGetDatabaseVersionWhenNotImplemented() throws Exception {
        when:
		def connection = Mock(DatabaseConnection)
		connection.getDatabaseMajorVersion() >> { throw new UnsupportedOperationException() }
        connection.getDatabaseMinorVersion() >> { throw new UnsupportedOperationException() }

		SybaseDatabase database = new SybaseDatabase()
		database.setConnection(connection)

        then:
        database.getDatabaseMajorVersion() == -1
        database.getDatabaseMinorVersion() == -1
	}

    def escapeIndexName() {
        when:
        def database = new SybaseDatabase()

        then:
        database.escapeIndexName(null, null, "index_name") == "index_name"
        database.escapeIndexName("cat", null, "index_name") == "index_name"
        database.escapeIndexName(null, "schem", "index_name") == "index_name"
        database.escapeIndexName("cat", "schem", "index_name") == "index_name"
    }
}
