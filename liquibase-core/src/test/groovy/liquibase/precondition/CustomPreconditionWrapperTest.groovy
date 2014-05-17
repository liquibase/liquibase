package liquibase.precondition

import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class CustomPreconditionWrapperTest extends Specification {

    @Shared
            resourceSupplier = new ResourceSupplier()

    def "load works correctly"() {
        when:
        def node = new ParsedNode(null, "customPrecondition")
                .addChild(null, "className", "liquibase.precondition.ExampleCustomPrecondition")
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        def precondition = new CustomPreconditionWrapper()
        try {
            precondition.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.classLoader != null
        precondition.className == "liquibase.precondition.ExampleCustomPrecondition"
        precondition.params.size() == 3
        precondition.getParamValue("param 1") == "param 1 value"
        precondition.getParamValue("param 2") == "param 2 value"
        precondition.getParamValue("param 3") == "param 3 value"

    }

    def "load handles params in a 'params' collection"() {
        when:
        def node = new ParsedNode(null, "customPrecondition")
                .addChild(null, "className", "liquibase.precondition.ExampleCustomPrecondition")
                .addChild(new ParsedNode(null, "params")
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        )
        def precondition = new CustomPreconditionWrapper()
        try {
            precondition.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.classLoader != null
        precondition.className == "liquibase.precondition.ExampleCustomPrecondition"
        precondition.params.size() == 3
        precondition.getParamValue("param 1") == "param 1 value"
        precondition.getParamValue("param 2") == "param 2 value"
        precondition.getParamValue("param 3") == "param 3 value"
    }

}
