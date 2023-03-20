package liquibase.snapshot

import liquibase.parser.core.yaml.YamlSnapshotParser
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Specification

class JsonSnapshotParserTest extends Specification {
    def "Test parsing a large file"() {
        when:
        YamlSnapshotParser parser = new YamlSnapshotParser()
        def snapshot = parser.parse("schemas-snapshot.json", new SearchPathResourceAccessor("target/test-classes"))

        then:
        noExceptionThrown()
        snapshot != null
    }
}
