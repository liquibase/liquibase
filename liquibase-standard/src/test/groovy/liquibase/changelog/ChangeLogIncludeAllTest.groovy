package liquibase.changelog


import spock.lang.Specification
import spock.lang.Unroll

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
