package liquibase.sql.visitor

import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

abstract class StandardSqlVisitorTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    @Unroll("#featureName setting #field")
    def "load works correctly"() {
        when:
        def visitor = createClass()
        def node = new ParsedNode(null, visitor.getSerializedObjectName())
        def fieldValue = "value for ${field}"
        node.addChild(null, field, fieldValue)
        try {
            visitor.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        visitor[field] == fieldValue

        where:
        field << createClass().getSerializableFields().findAll({ !(it in ["applyToRollback", "applicableDbms", "contexts", "labels"])})
    }

    def SqlVisitor createClass() {
        Class.forName(getClass().getName().replaceAll('Test$', "")).newInstance()
    }
}
