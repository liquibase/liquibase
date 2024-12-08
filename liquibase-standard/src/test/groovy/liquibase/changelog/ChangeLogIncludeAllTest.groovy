package liquibase.changelog

import liquibase.include.ChangeLogIncludeAll
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@Ignore
class ChangeLogIncludeAllTest extends Specification {

    @Unroll
    def getSerializableFields() {
        when:
        def changeLogIncludeAll = new ChangeLogIncludeAll()
        
        then:
        changeLogIncludeAll.getSerializableFields() != null
    }

    @Unroll
    def getSerializedObjectName() {
        when:
        def changeLogIncludeAll = new ChangeLogIncludeAll()
        then:
        changeLogIncludeAll.getSerializedObjectName() != null
    }

    def getSerializedObjectNamespace() {
        when:
        def changeLogIncludeAll = new ChangeLogIncludeAll()
        then:
        changeLogIncludeAll.getSerializedObjectNamespace() != null
    }
}
