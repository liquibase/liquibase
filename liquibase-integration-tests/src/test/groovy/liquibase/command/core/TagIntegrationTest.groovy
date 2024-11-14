package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class TagIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "validate that only the last change populates the tag field when multiple changes with the same tag value are applied"() {
        when:
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/tag-changelog.xml")
        updateCommand.execute()

        then:
        def detailsResultSet1 = h2.getConnection().createStatement()
                .executeQuery("select count(*) from databasechangelog where tag = 'release/1.0'")
        def detailsResultSet2 = h2.getConnection().createStatement()
                .executeQuery("select count(*) from databasechangelog where tag = 'release/0.1'")
        detailsResultSet1.next();
        detailsResultSet1.getInt(1) == 1
        detailsResultSet2.next();
        detailsResultSet2.getInt(1) == 1


        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "validate that the tag command clears tag fields for existing changes with the same tag value before applying"() {
        when:
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/tag-changelog.xml")
        updateCommand.execute()

        def tagCommand = new CommandScope(TagCommandStep.COMMAND_NAME)
        tagCommand.addArgumentValue(TagCommandStep.TAG_ARG, "release/1.0")
        tagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        tagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        tagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        tagCommand.execute()

        then:
        def detailsResultSet1 = h2.getConnection().createStatement()
                .executeQuery("select count(*) from databasechangelog where tag = 'release/1.0'")
        def detailsResultSet2 = h2.getConnection().createStatement()
                .executeQuery("select count(*) from databasechangelog where tag = 'release/1.0' and id = 'comment_change'")
        def detailsResultSet3 = h2.getConnection().createStatement()
                .executeQuery("select count(*) from databasechangelog where tag = 'release/0.1'")
        detailsResultSet1.next()
        detailsResultSet1.getInt(1) == 1
        detailsResultSet2.next()
        detailsResultSet2.getInt(1) == 1
        detailsResultSet3.next()
        detailsResultSet3.getInt(1) == 1

        cleanup:
        CommandUtil.runDropAll(h2)
    }


}
