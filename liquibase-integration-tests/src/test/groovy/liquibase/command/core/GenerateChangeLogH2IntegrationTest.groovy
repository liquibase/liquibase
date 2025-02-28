package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.util.CommandUtil
import liquibase.diff.output.changelog.ChangeGeneratorFactory
import liquibase.diff.output.changelog.core.MissingDataExternalFileChangeGenerator
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogH2IntegrationTest extends Specification{

    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "Ensure loadData csv file is processed from dataDir directory set"() {
        given:
        CommandUtil.runUpdate(db, "changelogs/h2/complete/loadData.test.changelog.xml", null, null, null)

        when:
        def outputFile = "output.changelog.file.xml"
        def dataDir = "testDataDir/"
        ChangeGeneratorFactory.getInstance().register(new MissingDataExternalFileChangeGenerator(dataDir))
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, "table,data")
        commandScope.addArgumentValue(DiffOutputControlCommandStep.DATA_OUTPUT_DIR_ARG, dataDir)
        commandScope.execute()

        then:
        def changelogFile = new File(outputFile)
        def changelogFileContent = FileUtil.getContents(changelogFile)
        changelogFileContent.containsIgnoreCase("file=\"testDataDir/")

        cleanup:
        changelogFile.delete()
        File testDir = new File(dataDir)
        if (testDir.exists()) {
            FileUtils.deleteDirectory(testDir)
        }
    }
}
