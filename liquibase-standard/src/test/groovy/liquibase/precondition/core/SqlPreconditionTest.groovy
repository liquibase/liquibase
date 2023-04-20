package liquibase.precondition.core

import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class SqlPreconditionTest extends Specification {
    @Shared resourceSupplier = new ResourceSupplier()

    def "load works with nested sql"() {
        when:
        def precondition = new SqlPrecondition()
        try {
            precondition.load(new liquibase.parser.core.ParsedNode(null, "sqlCheck").addChild(null, "expectedResult", "5").setValue("select count(*) from test"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.expectedResult == "5"
        precondition.sql == "select count(*) from test"
    }
}
