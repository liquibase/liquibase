package liquibase

import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Table
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class BitTypeIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("postgresql") as DatabaseTestSystem

    def "verify BIT column with numeric default generates correct SQL"() {
        given:
        def changeLogFile = "changelogs/pgsql/bit-numeric-default.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]

        when:
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()

        and: "BIT column has numeric default, not boolean"
        def database = postgres.getDatabaseFromFactory()
        def table = new Table(null, null, "bit_test_table")
        def snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(table, database, new SnapshotControl(database))
        def bitColumn = snapshot.getColumns().find { it.name == "bit_col" }

        bitColumn != null
        bitColumn.type.toString().toUpperCase().startsWith("BIT")
        bitColumn.defaultValue != null
        // Default should be 0 (Integer), not FALSE (Boolean)
        bitColumn.defaultValue == 0 || bitColumn.defaultValue.toString() == "0"
    }

    def "verify BIT column snapshot preserves numeric default"() {
        given:
        def changeLogFile = "changelogs/pgsql/bit-snapshot-test.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]

        when: "create table with BIT column"
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()

        when: "snapshot the table"
        def connection = postgres.getConnection()
        def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
        def table = new Table(null, null, "bit_snapshot_table")
        def snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(table, database, new SnapshotControl(database))
        def bitCol0 = snapshot.getColumns().find { it.name == "bit_col_0" }
        def bitCol1 = snapshot.getColumns().find { it.name == "bit_col_1" }

        then: "BIT columns maintain numeric defaults"
        bitCol0 != null
        bitCol0.defaultValue == 0 || bitCol0.defaultValue.toString() == "0"

        bitCol1 != null
        bitCol1.defaultValue == 1 || bitCol1.defaultValue.toString() == "1"
    }

    def "verify LoadData with boolean values into BIT column"() {
        given:
        def changeLogFile = "changelogs/pgsql/bit-loaddata-test.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]

        when:
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()

        and: "verify data was loaded correctly"
        def connection = postgres.getConnection()
        def stmt = connection.createStatement()
        def rs = stmt.executeQuery("SELECT id, active, flag FROM bit_loaddata_table ORDER BY id")

        rs.next()
        rs.getInt("id") == 1
        rs.getBoolean("active") == true  // "true" in CSV → 1 in BIT
        rs.getBoolean("flag") == false   // "false" in CSV → 0 in BIT

        rs.next()
        rs.getInt("id") == 2
        rs.getBoolean("active") == false // "0" in CSV → 0 in BIT
        rs.getBoolean("flag") == true    // "1" in CSV → 1 in BIT

        !rs.next()
        rs.close()
        stmt.close()
    }

    def "verify LoadData with various bit formats into BIT VARYING column"() {
        given:
        def changeLogFile = "changelogs/pgsql/bit-multi-loaddata-test.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]

        when:
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()

        and: "verify data was loaded correctly with various input formats"
        def connection = postgres.getConnection()
        def stmt = connection.createStatement()
        def rs = stmt.executeQuery("SELECT id, status_bits::text, flags FROM bit_multi_loaddata_table ORDER BY id")

        // Row 1: Binary string "101010" → B'101010'
        rs.next()
        rs.getInt("id") == 1
        rs.getString("status_bits") == "101010"
        rs.getString("flags") == "Binary string"

        // Row 2: Decimal "42" → B'101010' (binary representation of 42)
        rs.next()
        rs.getInt("id") == 2
        rs.getString("status_bits") == "101010"  // 42 in binary is 101010
        rs.getString("flags") == "Decimal integer"

        // Row 3: Existing literal "B'110011'" → B'110011' (no double-wrapping)
        rs.next()
        rs.getInt("id") == 3
        rs.getString("status_bits") == "110011"
        rs.getString("flags") == "Existing literal"

        // Row 4: Lowercase literal "b'111000'" → B'111000'
        rs.next()
        rs.getInt("id") == 4
        rs.getString("status_bits") == "111000"
        rs.getString("flags") == "Lowercase literal"

        // Row 5: Boolean "true" → B'1'
        rs.next()
        rs.getInt("id") == 5
        rs.getString("status_bits") == "1"
        rs.getString("flags") == "Boolean true"

        // Row 6: Boolean "false" → B'0'
        rs.next()
        rs.getInt("id") == 6
        rs.getString("status_bits") == "0"
        rs.getString("flags") == "Boolean false"

        // Row 7: Numeric "0" → B'0'
        rs.next()
        rs.getInt("id") == 7
        rs.getString("status_bits") == "0"
        rs.getString("flags") == "Zero"

        // Row 8: Numeric "1" → B'1'
        rs.next()
        rs.getInt("id") == 8
        rs.getString("status_bits") == "1"
        rs.getString("flags") == "One"

        !rs.next()
        rs.close()
        stmt.close()
    }
}