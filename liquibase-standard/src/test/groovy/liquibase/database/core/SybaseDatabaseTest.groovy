package liquibase.database.core

import liquibase.CatalogAndSchema
import liquibase.Scope
import liquibase.database.DatabaseConnection
import liquibase.exception.DatabaseException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.statement.core.GetViewDefinitionStatement
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

public class SybaseDatabaseTest extends Specification {

    def cleanup() {
        Scope.currentScope.getSingleton(ExecutorService.class).reset()
    }

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new SybaseDatabase().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        verifyAll {
            database.correctObjectName(objectName, objectType) == expectedCorrect
            database.escapeObjectName(objectName, objectType) == expectedEscape
        }

        where:
        quotingStrategy           || objectName       | objectType || expectedCorrect  | expectedEscape
        null                      || 'col1'           | Column     || 'col1'           | 'col1'
        null                      || 'COL1'           | Column     || 'COL1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'Col1'           | 'Col1'
        null                      || 'col with space' | Column     || 'col with space' | '[col with space]'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'col1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'COL1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'Col1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'col with space' | '[col with space]'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '[col1]'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '[COL1]'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '[Col1]'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '[col with space]'
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
            executor.queryForList(_ as GetViewDefinitionStatement, String.class) >> viewRows
            SybaseDatabase database = new SybaseDatabase()
            Scope.currentScope.getSingleton(ExecutorService.class).setExecutor("jdbc", database, executor)

        then:
            database.getViewDefinition(new CatalogAndSchema(null, "dbo"), "view_name") == expected

        where:
            viewRows | expected
            [] | ""
            ["foo"] | "foo"
            ["foo", " bar", " bat"] | "foo bar bat"
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
            connection.getDatabaseMajorVersion() >>  { -1 }
            connection.getDatabaseMinorVersion() >>  { -1 }

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

    def supportsInitiallyDeferrableColumns() {
        when:
        def database = new SybaseDatabase()

        then:
        database.supportsInitiallyDeferrableColumns() == false
    }

    def getCurrentDateTimeFunction() {
        when:
        def database = new SybaseDatabase()

        then:
        database.getCurrentDateTimeFunction() == "GETDATE()"
    }

    def testGetDefaultDriver() throws DatabaseException {
        when:
        def database = new SybaseDatabase()

        then:
        database.getDefaultDriver("jdbc:xsybase://localhost/liquibase")     == "com.sybase.jdbc4.jdbc.SybDriver"
        database.getDefaultDriver("jdbc:sybase:Tds://localhost/liquibase")  == "com.sybase.jdbc4.jdbc.SybDriver"
        database.getDefaultDriver("jdbc:jtds:sybase://localhost/liquibase") == "net.sourceforge.jtds.jdbc.Driver"
        database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase") == null
    }
}
