package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogMSSQLIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem mssql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mssql")

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated sql changelog"() {
        given:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(mssql,'output.mssql.sql')

        then:
        def outputFile = new File('output.mssql.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("COMMENT 1")
        contents.contains("COMMENT 2")
        contents.contains("COMMENT 3")
        contents.contains("COMMENT 4")
        contents.contains("COMMENT 5")
        contents.contains("COMMENT 6")

        when:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'output.mssql.sql')

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(mssql)
        outputFile.delete()
    }

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated xml/json/yml changelog"(String fileType) {
        given:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(mssql,"output.mssql.$fileType")

        then:
        def outputFile = new File("output.mssql.$fileType")
        def contents = FileUtil.getContents(outputFile)
        contents.count('columnParentType') == 2 //Should appear for the two view column comments.

        when:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql, "output.mssql.$fileType")

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(mssql)
        outputFile.delete()

        where:
        fileType << ['xml', 'json', 'yml']
    }

    def "Should include schema when generating changelog with view and using 'use-or-replace-option'"() {
        when:
        String changelogFile = "target/test-classes/changelogs/" + StringUtil.randomIdentifer(10) + "/formatted.mssql.sql"
        String updateChangelogFile = "target/test-classes/changelogs/" + StringUtil.randomIdentifer(10) + "/formatted.sql"

        def contents =
                """
-- liquibase formatted sql
  
-- changeset liquibase:1 label:basic
CREATE TABLE Employees (
    EmployeeID INT,
    FirstName NVARCHAR(50)
);
-- rollback drop table Employees

--changeset wesley:1575473414720-9 splitStatements:false
CREATE VIEW employees_view AS SELECT FirstName FROM [dbo].Employees;

"""
        File f = new File(updateChangelogFile)
        f.getParentFile().mkdirs()
        f.write(contents + "\n")

        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, updateChangelogFile)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mssql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mssql.getPassword())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mssql.getConnectionUrl())

        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mssql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mssql.getPassword())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mssql.getConnectionUrl())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.USE_OR_REPLACE_OPTION, true);
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        Map<String, Object> scopeValues = new HashMap<>()
        outputStream.flush()

        scopeValues.put("liquibase.pro.sql.inline", true)
        scopeValues.put(Scope.Attr.resourceAccessor.name(), new SearchPathResourceAccessor("."))
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                updateCommandScope.execute()
                commandScope.execute()
            }
        })
        def generatedChangelog = new File(changelogFile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)

        then:
        noExceptionThrown()
        generatedChangelogContents.contains("N'CREATE VIEW [employees_view] AS SELECT '")

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }

        CommandUtil.runDropAll(mssql)

        if (mssql.getConnection()) {
            mssql.getConnection().close()
        }
    }
}
