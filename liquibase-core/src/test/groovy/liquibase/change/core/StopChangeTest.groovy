package liquibase.change.core

import liquibase.parser.core.ParsedNode
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class StopChangeTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load sets message to string value"() {
        when:
        def change = new StopChange()
        change.load(new ParsedNode(null, "stop").setValue("stopping..."), resourceSupplier.simpleResourceAccessor)

        then:
        change.message == "stopping..."
    }
}
