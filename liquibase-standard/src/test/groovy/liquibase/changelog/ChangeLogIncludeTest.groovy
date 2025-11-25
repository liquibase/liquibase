package liquibase.changelog


import spock.lang.Specification
import spock.lang.Unroll

class ChangeLogIncludeTest extends Specification {

    @Unroll
    def "getSerializableFields includes logicalFilePath"() {
        when:
        def changeLogInclude = new ChangeLogInclude()

        then:
        changeLogInclude.getSerializableFields() != null
        changeLogInclude.getSerializableFields().contains("logicalFilePath")
    }

    def "logicalFilePath can be set and retrieved"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        changeLogInclude.setLogicalFilePath("my/logical/path")

        then:
        changeLogInclude.getLogicalFilePath() == "my/logical/path"
    }

    @Unroll
    def "getSerializedObjectName returns include"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        then:
        changeLogInclude.getSerializedObjectName() == "include"
    }

    def "getSerializedObjectNamespace returns standard namespace"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        then:
        changeLogInclude.getSerializedObjectNamespace() != null
    }
}

