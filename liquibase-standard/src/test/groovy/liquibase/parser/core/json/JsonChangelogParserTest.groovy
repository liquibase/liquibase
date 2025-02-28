package liquibase.parser.core.json

import liquibase.change.AbstractSQLChange
import liquibase.changelog.ChangeLogParameters
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class JsonChangelogParserTest extends Specification {
    def "Test parse with modifyChangeSets and stripComments"() {
        def path = "liquibase/parser/core/json/master_changelog.json"

        when:
        JsonChangeLogParser parser = new JsonChangeLogParser()
        def changeLog = parser.parse(path, new ChangeLogParameters(), new JUnitResourceAccessor());
        def changeSet = changeLog.changeSets[0]
        def change = changeSet.changes[0]

        then:
        assert change instanceof AbstractSQLChange
        assert ((AbstractSQLChange)change).isStripComments()
    }
}
