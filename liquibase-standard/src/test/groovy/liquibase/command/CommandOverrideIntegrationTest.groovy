package liquibase.command

import liquibase.Scope
import liquibase.command.core.OverrideTestBaseCommandStep
import liquibase.command.core.OverrideTestDefaultBaseCommandStep
import liquibase.database.Database
import liquibase.database.core.H2Database
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification

/**
 * Integration tests for CommandOverride with supportedDatabases.
 * Tests actual pipeline construction and execution with registered CommandSteps.
 */
class CommandOverrideIntegrationTest extends Specification {

    def setup() {
        OverrideTestBaseCommandStep.reset()
        OverrideTestDefaultBaseCommandStep.reset()
    }

    def "pipeline includes base step when only database-specific overrides exist"() {
        when: "Get command definition with database-specific overrides"
        def commandDef = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("overrideTest")

        then: "Pipeline should contain all overrides AND base step"
        commandDef.pipeline*.class*.simpleName.contains("OverrideTestBaseCommandStep")
        commandDef.pipeline*.class*.simpleName.contains("OverrideTestH2CommandStep")
        commandDef.pipeline*.class*.simpleName.contains("OverrideTestPostgresCommandStep")
    }

    def "pipeline excludes base step when default override exists"() {
        when: "Get command definition with default override"
        def commandDef = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("overrideTestDefault")

        then: "Pipeline should contain default override but NOT base step"
        commandDef.pipeline*.class*.simpleName.contains("OverrideTestDefaultCommandStepImpl")
        !commandDef.pipeline*.class*.simpleName.contains("OverrideTestDefaultBaseCommandStep")
    }

    def "execution with H2 database runs only H2 override"() {
        given: "CommandScope for overrideTest command"
        def commandScope = new CommandScope("overrideTest")
        def h2Database = new H2Database()
        commandScope.provideDependency(Database.class, h2Database)

        when: "Execute with H2 database"
        commandScope.execute()

        then: "Only H2 override should execute"
        OverrideTestBaseCommandStep.executionLog.size() == 1
        OverrideTestBaseCommandStep.executionLog[0] == "OverrideTestH2CommandStep"
    }

    def "execution with Postgres database runs only Postgres override"() {
        given: "CommandScope for overrideTest command"
        def commandScope = new CommandScope("overrideTest")
        def postgresDatabase = new PostgresDatabase()
        commandScope.provideDependency(Database.class, postgresDatabase)

        when: "Execute with Postgres database"
        commandScope.execute()

        then: "Only Postgres override should execute"
        OverrideTestBaseCommandStep.executionLog.size() == 1
        OverrideTestBaseCommandStep.executionLog[0] == "OverrideTestPostgresCommandStep"
    }

    def "execution with MySQL database runs base step when no override matches"() {
        given: "CommandScope for overrideTest command"
        def commandScope = new CommandScope("overrideTest")
        def mysqlDatabase = new liquibase.database.core.MySQLDatabase()
        commandScope.provideDependency(Database.class, mysqlDatabase)

        when: "Execute with MySQL database (no matching override)"
        commandScope.execute()

        then: "Base step should execute as fallback"
        OverrideTestBaseCommandStep.executionLog.size() == 1
        OverrideTestBaseCommandStep.executionLog[0] == "OverrideTestBaseCommandStep"
    }

    def "execution with default override runs override for any database"() {
        given: "CommandScope for overrideTestDefault command"
        def commandScope = new CommandScope("overrideTestDefault")
        def mysqlDatabase = new liquibase.database.core.MySQLDatabase()
        commandScope.provideDependency(Database.class, mysqlDatabase)

        when: "Execute with any database"
        commandScope.execute()

        then: "Default override should execute, base step should NOT be in pipeline"
        OverrideTestDefaultBaseCommandStep.executionLog.size() == 1
        OverrideTestDefaultBaseCommandStep.executionLog[0] == "OverrideTestDefaultCommandStep"
    }

    def "execution without database runs base step"() {
        given: "CommandScope for overrideTest command without database"
        def commandScope = new CommandScope("overrideTest")
        // Don't provide Database dependency

        when: "Execute without database"
        commandScope.execute()

        then: "Base step should execute (database-specific overrides skip when database is null)"
        OverrideTestBaseCommandStep.executionLog.size() == 1
        OverrideTestBaseCommandStep.executionLog[0] == "OverrideTestBaseCommandStep"
    }
}
