package liquibase.command.core.helpers

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.database.core.MockDatabase
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for {@link DatabaseChangelogCommandStep}, specifically the fix for issue #7602
 * where dbms-specific properties were not being correctly filtered when a user-provided
 * ChangeLogParameters was passed via CommandScope.
 */
class DatabaseChangelogCommandStepTest extends Specification {

    /**
     * Test that verifies the fix for issue #7602.
     * When a user creates ChangeLogParameters without a database and passes it via CommandScope,
     * the database filter should still be set correctly so that dbms-specific properties
     * in the changelog are resolved correctly.
     */
    def "user-provided ChangeLogParameters should have database filter set for dbms resolution"() {
        given: "A user-created ChangeLogParameters without a database, and a PostgreSQL database"
        def postgresDb = new PostgresDatabase()
        
        // User creates their own ChangeLogParameters (as described in issue #7602)
        def userParams = new ChangeLogParameters()
        userParams.set("description", "An DBMS specialized in storing and retrieving data.")
        
        // At this point, userParams.getDatabase() is null
        expect: "userParams has no database filter set"
        userParams.getDatabase() == null
        
        when: "getChangeLogParameters is called with user params and postgres database"
        def commandScope = new CommandScope("update")
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, userParams)
        
        def changeLogParams = Scope.instance((Scope.ScopedRunnerWithReturn<ChangeLogParameters>) {
            return getChangeLogParametersInternal(commandScope, postgresDb)
        })
        
        then: "the database filter should be set to postgres's short name"
        changeLogParams.getDatabase() == "postgresql"
        
        cleanup:
        postgresDb.close()
    }
    
    /**
     * Helper method that replicates the logic of getChangeLogParameters for testing.
     * This is needed because the method is private.
     */
    private ChangeLogParameters getChangeLogParametersInternal(CommandScope commandScope, liquibase.database.Database database) {
        ChangeLogParameters changeLogParameters = commandScope.getArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS)
        if (changeLogParameters == null) {
            changeLogParameters = new ChangeLogParameters(database)
            changeLogParameters.addJavaProperties()
            changeLogParameters.addDefaultFileProperties()
        } else {
            // This is the fix for issue #7602 - when a user-provided ChangeLogParameters is passed,
            // we still need to set the database filter so that dbms-specific properties
            // are correctly resolved.
            changeLogParameters.setDatabase(database.getShortName())
        }
        return changeLogParameters
    }
}