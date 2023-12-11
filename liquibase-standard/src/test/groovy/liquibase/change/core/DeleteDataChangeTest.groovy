package liquibase.change.core

import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class DeleteDataChangeTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load with whereParams"() {
        when:
        def change = new DeleteDataChange()
        def whereParams = new ParsedNode(null, "whereParams")
                .addChild(new ParsedNode(null, "param").addChild(null, "valueNumeric", "134"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "other_val", value: "asdf"]))
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "delete").addChild(null, "tableName", "deleteTest").addChild(whereParams), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "deleteTest"
        change.whereParams.size() == 2
        change.whereParams[0].valueNumeric == 134
        change.whereParams[1].name == "other_val"
        change.whereParams[1].value == "asdf"
    }

}
