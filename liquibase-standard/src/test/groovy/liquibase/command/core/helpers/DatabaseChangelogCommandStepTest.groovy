package liquibase.command.core.helpers

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.StandardChangeLogHistoryService
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Tests for {@link DatabaseChangelogCommandStep}, specifically the fix for issue #7602
 * where dbms-specific properties were not being correctly filtered when a user-provided
 * ChangeLogParameters was passed via CommandScope.
 */
class DatabaseChangelogCommandStepTest extends Specification {

    def "cleanUp only resets the ChangeLogHistoryService for its own database (regression for liquibase#6927)"() {
        // Reproduces bug 3 from https://github.com/liquibase/liquibase/issues/6927: this helper
        // step's cleanUp() used to call the blanket ChangeLogHistoryServiceFactory.resetAll(),
        // which - since the factory is a single JVM-wide singleton since #7877 - wiped out every
        // other concurrently-running Maven module's cached ChangeLogHistoryService too, not just
        // the finishing module's own. That combined badly with the fix for bug 1: once wiped, a
        // module's next lookup fell back to StandardChangeLogHistoryService (supports() always
        // true) instead of its own offline-aware service, producing a raw ClassCastException.
        // This bug only misfired in roughly 1 run in 15 under a real -T Maven build - too rare to
        // rely on a threaded/looping test, so this exercises cleanUp() directly for two "modules"
        // sharing the same JVM-wide factory instance instead.
        given: "two independent executions (as concurrently-running Maven modules would each have) with their own cached ChangeLogHistoryService"
        def factory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)
        factory.resetAll()
        def databaseA = new MockDatabase()
        def databaseB = new MockDatabase()
        def serviceA = new StandardChangeLogHistoryService()
        serviceA.setDatabase(databaseA)
        def serviceB = new StandardChangeLogHistoryService()
        serviceB.setDatabase(databaseB)
        factory.registerForDatabase(databaseA, serviceA)
        factory.registerForDatabase(databaseB, serviceB)

        def step = new DatabaseChangelogCommandStep()
        def commandA = new CommandScope(DatabaseChangelogCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, databaseA)
        def resultsBuilderA = new CommandResultsBuilder(commandA, new ByteArrayOutputStream())

        when: "module A finishes and cleans up while module B is still in flight"
        step.cleanUp(resultsBuilderA)

        then: "module B's cached service survives module A's cleanup"
        factory.getChangeLogService(databaseB).is(serviceB)

        and: "module A's own cached service was cleared, as expected"
        !factory.getChangeLogService(databaseA).is(serviceA)

        cleanup:
        factory.resetAll()
    }

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
