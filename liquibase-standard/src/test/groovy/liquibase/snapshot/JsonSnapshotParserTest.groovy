package liquibase.snapshot

import liquibase.Scope
import liquibase.diff.output.StandardObjectChangeFilter
import liquibase.parser.SnapshotParser
import liquibase.parser.SnapshotParserFactory
import liquibase.parser.core.yaml.YamlSnapshotParser
import liquibase.resource.SearchPathResourceAccessor
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
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

    def "Parse snapshot without filters"() {
        when:
        YamlSnapshotParser parser = new YamlSnapshotParser()
        def snapshot = parser.parse("schemas-snapshot.json", new SearchPathResourceAccessor("target/test-classes"))

        then:
        snapshot != null
        snapshot instanceof RestoredDatabaseSnapshot
        snapshot.get(Schema.class).size() == 5
    }

    def "Parse snapshot with excludeObjects filter in Scope"() {
        def objectChangeFilter = new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, "s1,s3")

        when:
        def snapshot = null
        Scope.child(["objectChangeFilter": objectChangeFilter], { ->
            YamlSnapshotParser parser = new YamlSnapshotParser()
            snapshot = parser.parse("schemas-snapshot.json", new SearchPathResourceAccessor("target/test-classes"))
        } as Scope.ScopedRunner)

        then:
        snapshot != null
        snapshot instanceof RestoredDatabaseSnapshot
        Set<Schema> schemas = snapshot.get(Schema.class)
        schemas.size() == 3
        schemas.find { it.name == "s1" } == null
        schemas.find { it.name == "s3" } == null
        schemas.find { it.name == "s2" } != null
        schemas.find { it.name == "s4" } != null
        schemas.find { it.name == "s5" } != null
    }

    def "Parse snapshot with snapshotTypes filter in Scope"() {
        def snapshotControl = new SnapshotControl(null, Schema.class)

        when:
        def snapshot = null
        Scope.child(["snapshotControl": snapshotControl], { ->
            YamlSnapshotParser parser = new YamlSnapshotParser()
            snapshot = parser.parse("schemas-snapshot.json", new SearchPathResourceAccessor("target/test-classes"))
        } as Scope.ScopedRunner)

        then:
        snapshot != null
        snapshot instanceof RestoredDatabaseSnapshot
        snapshot.get(Schema.class).size() == 5
        snapshot.get(Table.class).isEmpty()
        snapshot.get(Column.class).isEmpty()
        snapshot.get(View.class).isEmpty()
    }
}
