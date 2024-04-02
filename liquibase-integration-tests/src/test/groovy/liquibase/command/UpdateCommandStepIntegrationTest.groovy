package liquibase.command

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.command.core.GenerateChangelogCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.report.UpdateReportParameters
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateCommandStepIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "validate context and label entry has not been added previously"() {
        when:
        Contexts context = new Contexts("testContext")
        LabelExpression label = new LabelExpression("testLabel")
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check-to-not-deploy.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())

        then:
        !new UpdateCommandStep().isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)
    }

    def "validate context and label entry has been added previously"() {
        when:
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())
        Contexts context = new Contexts("testContext2")
        LabelExpression label = new LabelExpression("testLabel2")
        liquibase.update()

        then:
        new UpdateCommandStep().isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)
    }

    def "validate update is successfully executed even when there is by a context mismatch and a non-existent file is referenced in a changeSet"() {
        when:
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]
        def outputStream = new ByteArrayOutputStream()
        def commandResults = null
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, "test2")
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/update.changelog.yaml")
            commandScope.setOutput(outputStream)
            commandResults = commandScope.execute()
        } as Scope.ScopedRunner)

        then:
        outputStream.toString().contains("Run:                          1")
        outputStream.toString().contains("Filtered out:                 1")
        ((UpdateReportParameters) commandResults.getResult("updateReport")).getSuccess()
    }
}