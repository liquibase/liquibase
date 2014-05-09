package liquibase.change.core

import liquibase.parser.core.ParsedNode
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class LoadDataColumnConfigTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load includes base and custom parameters"() {
        when:
        def node = new ParsedNode(null, "column").addChildren([index: "5", header: "header_col", name: "col_name"])
        def column = new LoadDataColumnConfig()
        column.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        column.index== 5
        column.header== "header_col"
        column.name == "col_name"
    }

}
