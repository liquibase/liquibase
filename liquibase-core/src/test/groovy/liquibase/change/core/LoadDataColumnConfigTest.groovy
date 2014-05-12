package liquibase.change.core

import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class LoadDataColumnConfigTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load includes base and custom parameters"() {
        when:
        def node = new ParsedNode(null, "column").addChildren([index: "5", header: "header_col", name: "col_name"])
        def column = new LoadDataColumnConfig()
        try {
            column.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        column.index== 5
        column.header== "header_col"
        column.name == "col_name"
    }

}
