package liquibase.diffchangelog

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.DiffChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class DiffChangelogMssqlIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem mssql =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mssql")

    def "column direction is not included in addPrimaryKey change" () {
        def snapshotFilename = "target-${RandomStringUtils.randomAlphabetic(10)}.json"
        def snapshotFilepath = "target/test-classes/" + snapshotFilename
        def changelogFile = "target/test-classes/diffChangelog-${RandomStringUtils.randomAlphabetic(10)}.xml"
        when:
        mssql.executeSql("""
CREATE TABLE MyTest (
    ID INT not null,
    Name VARCHAR(50),
    Age INT);""")
        CommandUtil.runSnapshot(mssql, snapshotFilepath)
        mssql.executeSql("""
ALTER TABLE MyTest
ADD CONSTRAINT PK_MyTest PRIMARY KEY (ID DESC);""")
        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_URL_ARG, mssql.getConnectionUrl())
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_USERNAME_ARG, mssql.getUsername())
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_PASSWORD_ARG, mssql.getPassword())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, "offline:mssql?snapshot=" + snapshotFilename)

        then:
        commandScope.execute()
        def generatedChangelog = new File(changelogFile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)
        generatedChangelogContents.contains('<addPrimaryKey columnNames="ID" constraintName="PK_MyTest" tableName="MyTest"/>')

        cleanup:
        generatedChangelog.delete()
        FileUtils.forceDelete(new File(snapshotFilepath))
    }

}
