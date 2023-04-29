package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogMySQLIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    def setupSpec() {
        CommandUtil.runDropAll(mysql)
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
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
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
        CommandUtil.runDropAll(mysql)
        CommandUtil.runUpdate(mysql,"output.xml")

        then:
        noExceptionThrown()

        cleanup:
        outputFile.delete()
        new File("objects").deleteDir()
    }

    def "Ensure Enum Output On generatedChangelog"() throws Exception {
        given:
        CommandUtil.runUpdateWithTestChangelog(mysql,"changelogs/mysql/complete/enum.changelog.xml")

        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())

        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)

        when:
        commandScope.execute()

        then:
        noExceptionThrown()
        outputStream.toString().contains("ENUM('FAILED', 'CANCELLED', 'INGEST', 'IN_PROGRESS', 'COMPLETE')")
    }

    def "Ensure generated changelog map JSON columns correctly"() {
        given:
        mysql.executeSql("""create table foo(               
                id numeric not null primary key, 
                some_json json null)""")

        when:
        CommandUtil.runGenerateChangelog(mysql, "output.mysql.sql")
        def outputFile = new File("output.mysql.sql")
        def contents = FileUtil.getContents(outputFile)

        then:
        contents.contains("some_json JSON NULL")

        cleanup:
        outputFile.delete()
        CommandUtil.runDropAll(mysql)

    }
}

