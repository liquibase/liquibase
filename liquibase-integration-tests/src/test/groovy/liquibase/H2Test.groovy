package liquibase


import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.resource.SearchPathResourceAccessor
import org.apache.commons.lang3.StringUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class H2Test extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "verify sequence is not created again when precondition fails because it already exists"() {
        when:
        def changeLogFile = "changelogs/sequenceExists-h2.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()
    }

    def showRowsAffectedForDMLOnly() {
        when:
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child(Scope.Attr.logService.name(), bufferLog, () -> {
            CommandUtil.runUpdate(h2, "src/test/resources/changelogs/common/rows-affected.xml")
        })

        then:
        String logAsString = bufferLog.getLogAsString(Level.FINE)
        assert logAsString.contains("1 row(s) affected")
        assert ! logAsString.contains("-1 row(s) affected")
        // Expected: 2 original INSERTs + 5 new DML statements (2 INSERTs + 1 UPDATE + 2 DELETEs) = 7
        // Note: DDL statements (CREATE TABLE) and PL/SQL blocks should NOT show "rows affected"
        assert StringUtils.countMatches(logAsString, "row(s) affected") == 7
        // Verify case-insensitive matching works
        assert logAsString.contains("insert into TABLE1 (name, id) VALUES ('lowercase', '3')")
        assert logAsString.contains("Insert Into TABLE1 (name, id) VALUES ('mixedcase', '4')")
        assert logAsString.contains("update TABLE1 set role = 'admin' where id = '1'")
        assert logAsString.contains("delete from TABLE1 where id = '4'")
    }
}
