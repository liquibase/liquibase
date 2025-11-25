package liquibase.changelog

import liquibase.serializer.LiquibaseSerializable
import spock.lang.Specification

/**
 * Unit tests for {@link ChangeLogInclude} class.
 * <p>
 * These tests verify the functionality of the ChangeLogInclude class,
 * particularly focusing on the logicalFilePath attribute added in version 4.30.
 * </p>
 */
class ChangeLogIncludeTest extends Specification {

    /**
     * Tests that the logicalFilePath field is included in the set of serializable fields.
     * This ensures the field will be properly serialized and deserialized.
     */
    def "getSerializableFields includes logicalFilePath"() {
        when:
        def changeLogInclude = new ChangeLogInclude()

        then:
        changeLogInclude.getSerializableFields() != null
        changeLogInclude.getSerializableFields().contains("logicalFilePath")
    }

    /**
     * Tests that the logicalFilePath property can be set and retrieved correctly.
     * Verifies the getter and setter methods work as expected.
     */
    def "logicalFilePath can be set and retrieved"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        changeLogInclude.setLogicalFilePath("my/logical/path")

        then:
        changeLogInclude.getLogicalFilePath() == "my/logical/path"
    }

    /**
     * Tests that the serialized object name is correctly set to "include".
     * This is used when serializing the object to XML or other formats.
     */
    def "getSerializedObjectName returns include"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        then:
        changeLogInclude.getSerializedObjectName() == "include"
    }

    /**
     * Tests that the serialized object namespace is not null.
     * The namespace is used for XML serialization.
     */
    def "getSerializedObjectNamespace returns standard namespace"() {
        when:
        def changeLogInclude = new ChangeLogInclude()
        then:
        changeLogInclude.getSerializedObjectNamespace() != null
    }
}

