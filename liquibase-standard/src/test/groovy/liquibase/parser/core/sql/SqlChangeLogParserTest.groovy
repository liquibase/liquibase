package liquibase.parser.core.sql

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import spock.lang.Specification

import java.lang.reflect.Method

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE

class SqlChangeLogParserTest extends Specification {

    ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory =
            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)

    ChangeLogHistoryService changeLogHistoryService

    Database database = new MockDatabase()

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
        def result = invokeGenerateId(parser, "path/to/file.sql")

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
        def result = invokeGenerateId(parser, "path/to/file.sql")

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
        def result = invokeGenerateId(parser, "path/to/file.sql")

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
        def result = invokeGenerateId(parser, "path/to/file.sql")

        then:
        result == "raw"
    }

    def "getRanChangeSets is only queried once across repeated generateId calls on the same Database"() {
        given:
        def parser = new SqlChangeLogParser()

        when:
        def r1 = invokeGenerateId(parser, "dir/a.sql")
        def r2 = invokeGenerateId(parser, "dir/b.sql")
        def r3 = invokeGenerateId(parser, "dir/a.sql")

        then:
        r1 == "raw"
        r2 == "raw"
        r3 == "raw"
        1 * changeLogHistoryService.getRanChangeSets() >> []
    }

    private Object invokeGenerateId(SqlChangeLogParser parser, String path) {
        Method m = SqlChangeLogParser.class.getDeclaredMethod("generateId", String.class, Database.class)
        m.setAccessible(true)
        return m.invoke(parser, path, database)
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
