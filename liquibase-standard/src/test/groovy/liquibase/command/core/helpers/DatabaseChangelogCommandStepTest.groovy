package liquibase.command.core.helpers

import liquibase.changelog.ChangeLogParameters
import liquibase.command.CommandScope
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification

import java.lang.reflect.Method

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
        
        // Use reflection to call the actual private method
        Method method = DatabaseChangelogCommandStep.getDeclaredMethod(
            "getChangeLogParameters",
            CommandScope,
            liquibase.database.Database
        )
        method.setAccessible(true)
        
        def step = new DatabaseChangelogCommandStep()
        def changeLogParams = (ChangeLogParameters) method.invoke(step, commandScope, postgresDb)
        
        then: "the database filter should be set to postgres's short name"
        changeLogParams.getDatabase() == "postgresql"
        
        cleanup:
        postgresDb.close()
    }
}
