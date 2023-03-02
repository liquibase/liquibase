package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogMySQLCommandTest extends Specification {

    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    def setupSpec() {
        mysql.executeSql("""
create table str4 (
    col1 int ,
    col2 int auto_increment,
    col3 int,
    primary key(col2, col1)
)
""")
    }

    def cleanupSpec() {
        try {
            mysql.executeSql("""
drop table str4;
""")
        } catch(Exception ignored) {
        }
    }

    def "Ensure that MySQL generated changelog puts primary keys in as part of the create table change, even if the primary key is in a different order than the columns in the table" () {
        given:
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, "output.xml")

        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)

        when:
        commandScope.execute()

        then:
        def outputFile = new File("output.xml")
        def contents = FileUtil.getContents(outputFile)
        contents.count("<changeSet") == 1
        contents.count("primaryKey=\"true\"") == 2
        contents.count("<addPrimaryKey") == 0
        // drop the table before we create it again in the update below
        !mysql.executeSql("drop table str4")

        when:
        runUpdate()

        then:
        noExceptionThrown()

        cleanup:
        outputFile.delete()
        new File("objects").deleteDir()
    }

    private void runUpdate() {
        UpdateCommandStep step = new UpdateCommandStep()

        CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, mysql.getPassword())
        commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "output.xml")

        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }
}

