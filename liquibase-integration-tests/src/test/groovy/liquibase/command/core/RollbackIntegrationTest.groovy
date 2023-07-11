package liquibase.command.core


import liquibase.Scope
import liquibase.TagVersionEnum
import liquibase.command.util.CommandUtil
import liquibase.exception.CommandExecutionException
import liquibase.exception.RollbackFailedException
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.resource.SearchPathResourceAccessor
import liquibase.ui.ConsoleUIService
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class RollbackIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should rollback correctly with duplicate tags"() {
        given:
        def changelogFile = 'target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml'
        CommandUtil.runUpdate(postgres, changelogFile)
        ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
        def outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))

        when:
        CommandUtil.runRollback(new SearchPathResourceAccessor("."), postgres, changelogFile, "version_2.0", TagVersionEnum.NEWEST)
        String outputString = outputStream.toString()

        then:
        noExceptionThrown()
        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::4b::createTable::Liquibase Pro User")
        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::13.2::testuser")

        when:
        CommandUtil.runUpdate(postgres, changelogFile)
        outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))
        CommandUtil.runRollback(new SearchPathResourceAccessor("."), postgres, changelogFile, "version_2.0", TagVersionEnum.OLDEST)
        outputString = outputStream.toString()

        then:
        noExceptionThrown()
        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::4a::createTable::Liquibase Pro User")
        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::13.1::testuser")

        when:
        outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))
        CommandUtil.runRollback(new SearchPathResourceAccessor("."), postgres, changelogFile, "version_2.0")
        outputString = outputStream.toString()

        then:
        def e = thrown(CommandExecutionException)
        assert e.getCause() instanceof RollbackFailedException
        assert e.getMessage() == "liquibase.exception.RollbackFailedException: Could not find tag 'version_2.0' in the database"

        cleanup:
        CommandUtil.runDropAll(postgres)
    }
}
