package liquibase.command

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.changelog.FastCheckService
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.executor.ExecutorService
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.report.UpdateReportParameters
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.statement.core.RawParameterizedSqlStatement
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
        !Scope.currentScope.getSingleton(FastCheckService.class).isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)
    }

    def "validate context and label entry has been added previously"() {
        when:
        Liquibase liquibase = new Liquibase("liquibase/test-changelog-fast-check.xml", new ClassLoaderResourceAccessor(),
                h2.getDatabaseFromFactory())
        Contexts context = new Contexts("testContext2")
        LabelExpression label = new LabelExpression("testLabel2")
        liquibase.update()

        then:
        Scope.currentScope.getSingleton(FastCheckService.class).isUpToDateFastCheck(null, h2.getDatabaseFromFactory(), liquibase.getDatabaseChangeLog(), context, label)
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

    def "Make sure there are not duplicated entries in DBCL for a given changeSet"() {
        when:
        def changeLogFile = "src/test/resources/changelogs/runAlways.set.on.changeset.and.onFail.precondition.xml"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(h2, changeLogFile)
            CommandUtil.runUpdate(h2, changeLogFile)
        } as Scope.ScopedRunner)


        def dbclEntriesCountQuery = "SELECT COUNT(*) FROM DATABASECHANGELOG WHERE ID = ? AND AUTHOR = ? AND FILENAME = ?;"
        def entriesFound = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", h2.getDatabaseFromFactory()).queryForInt(new RawParameterizedSqlStatement(dbclEntriesCountQuery, "DBCLDuplicatedEntriesTest", "mallod", changeLogFile))
        def dbclExecTypeQuery = "SELECT exectype FROM DATABASECHANGELOG WHERE ID = ? AND AUTHOR = ? AND FILENAME = ?;"
        def changeSetExecType= Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", h2.getDatabaseFromFactory()).queryForObject(new RawParameterizedSqlStatement(dbclExecTypeQuery, "DBCLDuplicatedEntriesTest", "mallod", changeLogFile), String.class)

        then:
        entriesFound == 1
        changeSetExecType.equals(ChangeSet.ExecType.RERAN.value)
    }
}
