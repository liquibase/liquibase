package liquibase.command

import liquibase.database.Database
import liquibase.database.core.H2Database
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification

/**
 * Tests the runtime filtering logic for CommandOverride with supportedDatabases.
 */
class CommandOverrideFilteringTest extends Specification {

    def "shouldExecuteOverride returns true when database matches supportedDatabases"() {
        given:
        def override = new TestH2Override()
        def h2Database = new H2Database()

        when:
        def annotation = override.getClass().getAnnotation(CommandOverride.class)
        def supportedDatabases = annotation.supportedDatabases()

        boolean matches = false
        for (Class<? extends Database> dbClass : supportedDatabases) {
            if (dbClass.isAssignableFrom(h2Database.getClass())) {
                matches = true
                break
            }
        }

        then:
        matches == true
        supportedDatabases.length == 1
        supportedDatabases[0] == H2Database.class
    }

    def "shouldExecuteOverride returns false when database doesn't match supportedDatabases"() {
        given:
        def override = new TestH2Override()
        def postgresDatabase = new PostgresDatabase()

        when:
        def annotation = override.getClass().getAnnotation(CommandOverride.class)
        def supportedDatabases = annotation.supportedDatabases()

        boolean matches = false
        for (Class<? extends Database> dbClass : supportedDatabases) {
            if (dbClass.isAssignableFrom(postgresDatabase.getClass())) {
                matches = true
                break
            }
        }

        then:
        matches == false
    }

    def "empty supportedDatabases acts as default for all databases"() {
        given:
        def override = new TestDefaultOverride()

        when:
        def annotation = override.getClass().getAnnotation(CommandOverride.class)
        def supportedDatabases = annotation.supportedDatabases()

        then:
        supportedDatabases.length == 0
    }

    def "multiple overrides can have different supportedDatabases"() {
        given:
        def h2Override = new TestH2Override()
        def postgresOverride = new TestPostgresOverride()

        when:
        def h2Annotation = h2Override.getClass().getAnnotation(CommandOverride.class)
        def postgresAnnotation = postgresOverride.getClass().getAnnotation(CommandOverride.class)

        then:
        h2Annotation.supportedDatabases()[0] == H2Database.class
        postgresAnnotation.supportedDatabases()[0] == PostgresDatabase.class
        h2Annotation.override() == TestBaseCommandStep2.class
        postgresAnnotation.override() == TestBaseCommandStep2.class
    }
}

class TestBaseCommandStep2 implements CommandStep {
    @Override
    String[][] defineCommandNames() { return null }

    @Override
    int getOrder(CommandDefinition commandDefinition) { return -1 }

    @Override
    void adjustCommandDefinition(CommandDefinition commandDefinition) {}

    @Override
    void validate(CommandScope commandScope) {}

    @Override
    void run(CommandResultsBuilder resultsBuilder) {}

    @Override
    List<Class<?>> requiredDependencies() { return null }

    @Override
    List<Class<?>> providedDependencies() { return null }
}

@CommandOverride(override = TestBaseCommandStep2.class, supportedDatabases = [H2Database.class])
class TestH2Override extends TestBaseCommandStep2 {}

@CommandOverride(override = TestBaseCommandStep2.class, supportedDatabases = [PostgresDatabase.class])
class TestPostgresOverride extends TestBaseCommandStep2 {}

@CommandOverride(override = TestBaseCommandStep2.class)
class TestDefaultOverride extends TestBaseCommandStep2 {}
