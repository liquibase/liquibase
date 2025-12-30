package liquibase.command

import liquibase.Scope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.executor.ExecutorService
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.statement.core.RawParameterizedSqlStatement
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class SearchPathIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope()
            .getSingleton(TestSystemFactory.class)
            .getTestSystem("postgresql")

    def "update command works with custom defaultSchemaName for PostgreSQL"() {
        given:
        def customSchema = "test_schema_" + System.currentTimeMillis()
        def database = postgres.getDatabaseFromFactory()
        def connection = database.getConnection()
        def statement = connection.createStatement()
        statement.execute("CREATE SCHEMA IF NOT EXISTS " + customSchema)
        connection.commit()

        when:
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, customSchema)
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/complete/simple.changelog.xml")
            commandScope.execute()
        } as Scope.ScopedRunner)

        then:
        noExceptionThrown()

        // Verify that tables were created in the custom schema
        def rs = connection.createStatement().executeQuery(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${customSchema}' AND table_name = 'databasechangelog'"
        )
        rs.next()
        rs.getInt(1) == 1

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS " + customSchema + " CASCADE")
        } catch (Exception e) {}
    }

    def "procedure creation works with custom schema for PostgreSQL"() {
        given:
        def customSchema = "proc_schema_" + System.currentTimeMillis()
        def database = postgres.getDatabaseFromFactory()
        def connection = database.getConnection()
        def statement = connection.createStatement()
        statement.execute("CREATE SCHEMA IF NOT EXISTS " + customSchema)
        connection.commit()

        when:
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, customSchema)
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/update/createProcedureReplaceIfExists.xml")
            commandScope.execute()
        } as Scope.ScopedRunner)

        then:
        noExceptionThrown()

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS " + customSchema + " CASCADE")
        } catch (Exception e) {}
    }

    def "SET LOCAL search_path reverts after transaction completes"() {
        given:
        def customSchema = "local_test_" + System.currentTimeMillis()
        def database = postgres.getDatabaseFromFactory()
        def connection = database.getConnection()
        def statement = connection.createStatement()
        statement.execute("CREATE SCHEMA IF NOT EXISTS " + customSchema)
        connection.commit()

        // Get the original search_path before any Liquibase operations
        def executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
        def originalSearchPath = executor.queryForObject(new RawParameterizedSqlStatement("SHOW SEARCH_PATH"), String.class)

        when:
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor,
        ]

        // Run Liquibase update with custom schema (which will use SET LOCAL SEARCH_PATH)
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG, customSchema)
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/complete/simple.changelog.xml")
            commandScope.execute()
        } as Scope.ScopedRunner)

        // After the transaction completes, check that search_path has reverted
        def currentSearchPath = executor.queryForObject(new RawParameterizedSqlStatement("SHOW SEARCH_PATH"), String.class)

        then:
        // The search_path should have reverted to the original value
        // This proves that SET LOCAL was used instead of persistent SET
        // (If persistent SET was used, the customSchema would still be in search_path)
        currentSearchPath == originalSearchPath ||
            currentSearchPath.startsWith("\"\$user\"") ||
            currentSearchPath.startsWith("\"$postgres.username\"")

        // customSchema should NOT be in current path - this proves SET LOCAL worked
        !currentSearchPath.contains(customSchema)

        cleanup:
        try {
            connection.createStatement().execute("DROP SCHEMA IF EXISTS " + customSchema + " CASCADE")
        } catch (Exception e) {}
    }
}
