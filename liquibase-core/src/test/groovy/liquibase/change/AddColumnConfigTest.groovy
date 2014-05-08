package liquibase.change

import liquibase.parser.core.ParsedNode
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class AddColumnConfigTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load includes base and custom parameters"() {
        when:
        def node = new ParsedNode(null, "column").addChildren([beforeColumn: "before_col", afterColumn: "after_col", position: 4, name: "col_name"])
        def column = new AddColumnConfig()
        column.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        column.beforeColumn == "before_col"
        column.afterColumn == "after_col"
        column.position == 4
        column.name == "col_name"
    }
}
