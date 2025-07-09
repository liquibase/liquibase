package liquibase.command

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.command.core.ValidateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
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
        def changeLogFileName = String.format("target/test-classes/liquibase/validate-test.%s", changelogFormat)
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")

        Map<String, Object> scopeSettings = [
                (GlobalConfiguration.STRICT.getKey()): true,
                ((Scope.Attr.resourceAccessor.name())): resourceAccessor]

        Scope.child(scopeSettings, {
            def validateCommand = new CommandScope(ValidateCommandStep.COMMAND_NAME)
            validateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
            validateCommand.addArgumentValue(ValidateCommandStep.CHANGELOG_FILE_ARG, changeLogFileName)
            def outputStream = new ByteArrayOutputStream()
            validateCommand.setOutput(outputStream)
            result = validateCommand.execute()
        } as Scope.ScopedRunner)

        then:
        final liquibase.exception.CommandExecutionException exception = thrown()
        exception.message.contains("Execution cannot continue because validation errors have been found")
        exception.message.contains("labels value cannot be empty while on Strict mode")
        exception.message.contains("context value cannot be empty while on Strict mode")
        exception.message.contains("runWith value cannot be empty while on Strict mode")
        exception.message.contains("dbms value cannot be empty while on Strict mode")
        exception.message.contains("logicalFilePath value cannot be empty while on Strict mode")

        cleanup:
        h2.getConnection().close()

        where:
        changelogFormat << ["sql", "xml"]
    }
}
