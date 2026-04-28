package liquibase.parser.core.sql

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.core.MockDatabase
import spock.lang.Specification

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE

class SqlChangeLogParserTest extends Specification {

    ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory =
            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)

    ChangeLogHistoryService changeLogHistoryService

    // Connection set to null so SnapshotGeneratorFactory returns an EmptyDatabaseSnapshot,
    // which lets isOldFormat() return false without trying real JDBC metadata calls.
    Database database = new MockDatabase().tap { it.setConnection((DatabaseConnection) null) }

    def setup() {
        changeLogHistoryService = Mock(ChangeLogHistoryService)
        changeLogHistoryService.supports(database) >> true
        changeLogHistoryService.getPriority() >> PRIORITY_DATABASE
        changeLogHistoryServiceFactory.register(changeLogHistoryService)
    }

    void cleanup() {
        changeLogHistoryServiceFactory.unregister(changeLogHistoryService)
    }

    def "generateId returns 'raw' when no ran changeset matches"() {
        given:
        changeLogHistoryService.getRanChangeSets() >> []
        def parser = new SqlChangeLogParser()

        when:
        def result = parser.generateId("path/to/file.sql", database)

        then:
        result == "raw"
    }

    def "generateId returns the interim id when a matching ran changeset exists"() {
        given:
        changeLogHistoryService.getRanChangeSets() >> [
                ranChangeSet("path/to/file.sql", "raw_path_to_file.sql", "includeAll")
        ]
        def parser = new SqlChangeLogParser()

        when:
        def result = parser.generateId("path/to/file.sql", database)

        then:
        result == "raw_path_to_file.sql"
    }

    def "generateId ignores ran changesets whose id does not match the interim id the filter would have computed"() {
        given: "a ran changeset for the same path but with an unrelated id"
        changeLogHistoryService.getRanChangeSets() >> [
                ranChangeSet("path/to/file.sql", "some_other_id", "includeAll")
        ]
        def parser = new SqlChangeLogParser()

        when:
        def result = parser.generateId("path/to/file.sql", database)

        then: "behavior matches the pre-cache code: fall back to 'raw'"
        result == "raw"
    }

    def "generateId ignores ran changesets whose author is not 'includeAll'"() {
        given:
        changeLogHistoryService.getRanChangeSets() >> [
                ranChangeSet("path/to/file.sql", "raw_path_to_file.sql", "someone-else")
        ]
        def parser = new SqlChangeLogParser()

        when:
        def result = parser.generateId("path/to/file.sql", database)

        then:
        result == "raw"
    }

    def "getRanChangeSets is only queried once across repeated generateId calls on the same Database"() {
        given:
        def parser = new SqlChangeLogParser()

        when:
        def r1 = parser.generateId("dir/a.sql", database)
        def r2 = parser.generateId("dir/b.sql", database)
        def r3 = parser.generateId("dir/a.sql", database)

        then:
        r1 == "raw"
        r2 == "raw"
        r3 == "raw"
        1 * changeLogHistoryService.getRanChangeSets() >> []
    }

    private static RanChangeSet ranChangeSet(String changeLog, String id, String author) {
        return new RanChangeSet(
                changeLog,
                id,
                author,
                null,
                new Date(),
                null,
                ChangeSet.ExecType.EXECUTED,
                "",
                "",
                null,
                null,
                "dep-id"
        )
    }
}
