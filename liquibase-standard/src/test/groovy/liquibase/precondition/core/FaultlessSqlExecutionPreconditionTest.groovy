package liquibase.precondition.core

import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class FaultlessSqlExecutionPreconditionTest extends Specification {
    @Shared resourceSupplier = new ResourceSupplier()

    def "load works with nested sql"() {
        when:
        def precondition = new FaultlessSqlExecutionPrecondition()
        try {
            precondition.load(new liquibase.parser.core.ParsedNode(null, "faultlessSqlExecution").setValue("SET ROLE DBADMIN"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.sql == "SET ROLE DBADMIN"
    }
}
