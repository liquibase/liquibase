package liquibase.command

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.command.core.ValidateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class ValidateCommandStepIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    @Unroll
    def "Validate changeset properties of a #changelogFormat format changelog cannot be set with an empty while on STRICT mode"(){
        when:
        def result
        def changeLogFileName = String.format("liquibase/validate-test.%s", changelogFormat)

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            def validateCommand = new CommandScope(ValidateCommandStep.COMMAND_NAME)
            validateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
            validateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFileName)
            def outputStream = new ByteArrayOutputStream()
            validateCommand.setOutput(outputStream)
            result = validateCommand.execute()
        })

        then:
        final liquibase.exception.CommandExecutionException exception = thrown()
        exception.message.contains("Execution cannot continue because validation errors have been found")
        exception.message.contains("- Property: labels Error: labels value cannot be empty while on Strict mode")
        exception.message.contains("- Property: context Error: context value cannot be empty while on Strict mode")
        exception.message.contains("- Property: runWith Error: runWith value cannot be empty while on Strict mode")
        exception.message.contains("- Property: dbms Error: dbms value cannot be empty while on Strict mode")

        cleanup:
        h2.getConnection().close()

        where:
        changelogFormat << ["sql", "xml"]
    }
}
