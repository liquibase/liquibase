package liquibase.change

import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class AddColumnConfigTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load includes base and custom parameters"() {
        when:
        def node = new ParsedNode(null, "column").addChildren([beforeColumn: "before_col", afterColumn: "after_col", position: 4, name: "col_name"])
        def column = new AddColumnConfig()
        try {
            column.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        column.beforeColumn == "before_col"
        column.afterColumn == "after_col"
        column.position == 4
        column.name == "col_name"
    }
}
