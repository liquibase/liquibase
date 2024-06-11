package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.exception.CommandExecutionException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogMySQLIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    def setup() {
        CommandUtil.runDropAll(mysql)
        def sql = """
create table str4 (
    col1 int ,
    col2 int auto_increment,
    col3 int,
    primary key(col2, col1)
)
"""
        def updateChangelogFile = "target/test-classes/create-table-" + StringUtil.randomIdentifier(10) + ".sql"
        File updateFile = new File(updateChangelogFile)
        updateFile.write(sql.toString())
        CommandUtil.runUpdate(mysql, updateChangelogFile)
    }

    def cleanupSpec() {
        CommandUtil.runDropAll(mysql)
    }

    def "Ensure that MySQL generated changelog puts primary keys in as part of the create table change, even if the primary key is in a different order than the columns in the table" () {
        given:
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mysql.getPassword())
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
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mysql.getPassword())

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

    def "Ensure generate changelog set runOnChange and replaceIfExists properties correctly for a created view changeset"() {
        given:
        CommandUtil.runUpdate(mysql, "changelogs/mysql/complete/createtable.and.view.changelog.xml", null, null, null)
        OutputStream outputStream = new ByteArrayOutputStream()

        when:
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mysql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mysql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mysql.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG, "createView")
        commandScope.addArgumentValue(GenerateChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG, "createView")
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputContent = outputStream.toString();
        outputContent.contains(" runOnChange=\"true\">")
        outputContent.contains(" replaceIfExists=\"true\"")

        cleanup:
        CommandUtil.runDropAll(mysql)
    }

    def "Ensure generated changelog SQL format contains 'OR REPLACE' instruction for a view when USE_OR_REPLACE_OPTION is set as true"() {
        given:
        mysql.executeSql("""create table foo(               
                id numeric not null primary key, 
                some_json json null)""")
        mysql.executeSql("CREATE VIEW fooView AS Select * from foo;")

        when:
        runGenerateChangelog(mysql, "output.mysql.sql", true)
        def outputFile = new File("output.mysql.sql")
        def contents = FileUtil.getContents(outputFile)

        then:
        contents.contains("CREATE OR REPLACE VIEW fooView")

        cleanup:
        outputFile.delete()
        CommandUtil.runDropAll(mysql)
    }

    def "Ensure generated changelog SQL format does NOT contain 'OR REPLACE' instruction for a view when USE_OR_REPLACE_OPTION is set as false"() {
        given:
        mysql.executeSql("""create table foo(               
                id numeric not null primary key, 
                some_json json null)""")
        mysql.executeSql("CREATE VIEW fooView AS Select * from foo;")

        when:
        runGenerateChangelog(mysql, "output.mysql.sql", false)
        def outputFile = new File("output.mysql.sql")
        def contents = FileUtil.getContents(outputFile)

        then:
        !contents.contains("CREATE OR REPLACE VIEW fooView")
        contents.contains("CREATE VIEW fooView")

        cleanup:
        outputFile.delete()
        CommandUtil.runDropAll(mysql)
    }

    static void runGenerateChangelog(DatabaseTestSystem db, String outputFile, boolean useOrReplaceOption) throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.USE_OR_REPLACE_OPTION, useOrReplaceOption)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()
    }
}

