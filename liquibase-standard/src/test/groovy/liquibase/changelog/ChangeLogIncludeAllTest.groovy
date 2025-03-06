package liquibase.changelog

import liquibase.include.StandardIncludeService
import liquibase.parser.core.ParsedNode
import liquibase.sdk.resource.MockResourceAccessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChangeLogIncludeAllTest extends Specification {

    @Shared
    def includeAll = new StandardIncludeService()
            .createChangelogIncludeAll(new ParsedNode(null, "includeAll").addChildren([path: "testPath"]),
                    new MockResourceAccessor(), null, new HashMap<String, Object>())

    @Unroll
    def getSerializableFields() {
        when:
        def changeLogIncludeAll = includeAll
        
        then:
        changeLogIncludeAll.getSerializableFields() != null
    }

    @Unroll
    def getSerializedObjectName() {
        when:
        def changeLogIncludeAll = includeAll
        then:
        changeLogIncludeAll.getSerializedObjectName() != null
    }

    def getSerializedObjectNamespace() {
        when:
        def changeLogIncludeAll = includeAll
        then:
        changeLogIncludeAll.getSerializedObjectNamespace() != null
    }
}
