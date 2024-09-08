package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UnexpectedChangeSetIntegrationTest extends Specification{

    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    def "validate there is no unexpected changeset reported after an initial tag in the database" () {
        given:
        CommandUtil.runTag(mysql,"TestTag")
        OutputStream outputStream =  new ByteArrayOutputStream();
        CommandUtil.runUnexpectedChangeSet(mysql,"changelogs/mysql/complete/createtable.and.view.changelog.xml", outputStream)

        expect:
        outputStream.toString().contains("contains no unexpected changes")
    }
}
