package liquibase.change.core

import liquibase.change.VariableConfig
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.serializer.LiquibaseSerializable
import spock.lang.Shared
import spock.lang.Specification

class VariableConfigTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load includes name parameter"() {
        when:
        def node = new ParsedNode(null, "variable").addChildren(name: "variable_name")
        def variable = new VariableConfig()
        try {
            variable.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        variable.getName() == "variable_name"
        variable.getSerializedObjectName() == "variable"
        variable.getSerializableFieldType("field") == LiquibaseSerializable.SerializationType.NAMED_FIELD
        variable.getSerializedObjectNamespace() == LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE
    }
}
