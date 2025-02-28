package liquibase.snapshot

import liquibase.Scope
import liquibase.parser.SnapshotParser
import liquibase.parser.SnapshotParserFactory
import liquibase.parser.core.yaml.YamlSnapshotParser
import liquibase.resource.SearchPathResourceAccessor
import liquibase.structure.core.Index
import liquibase.structure.core.View
import spock.lang.Specification
import spock.lang.Unroll

class JsonSnapshotParserTest extends Specification {
    def "Test parsing a large file"() {
        when:
        YamlSnapshotParser parser = new YamlSnapshotParser()
        def snapshot = parser.parse("schemas-snapshot.json", new SearchPathResourceAccessor("target/test-classes"))

        then:
        noExceptionThrown()
        snapshot != null
    }

    @Unroll
    def "Correctly parse a snapshot that contains Index objects on Views"() {
        def snapshotFile = "snapshot-with-index-views.json"
        when:
        SnapshotParser parser = SnapshotParserFactory.getInstance().getParser(snapshotFile, Scope.getCurrentScope().getResourceAccessor())
        DatabaseSnapshot parsedSnapshot = parser.parse(snapshotFile, Scope.getCurrentScope().getResourceAccessor())

        then:
        parsedSnapshot != null
        Set<Index> indexes = parsedSnapshot.get(Index.class)
        indexes[110]
        indexes[110].getTable() == null
        indexes[110].getRelation() instanceof View
    }
}
