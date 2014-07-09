package liquibase.sql.visitor

import liquibase.ContextExpression
import liquibase.action.visitor.ActionVisitor
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

abstract class StandardActionVisitorTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    @Unroll("#featureName setting #field")
    def "load works correctly"() {
        when:
        def visitor = createClass()
        def node = new ParsedNode(null, visitor.getSerializedObjectName())
        def fieldValue = "value for ${field}"
        def expected = null
        if (field in ["applyToRollback", "applyToUpdate"]) {
            fieldValue = true
        } else if (field == "dbms") {
            fieldValue = "oracle, mock"
            expected = "[mock, oracle]"
        } else if (field == "contexts") {
            fieldValue = "test, prod"
            expected = "(prod), (test)"
        }
        if (expected == null) {
            expected = fieldValue
        }

        node.addChild(null, field, fieldValue)
        visitor.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        visitor[field].toString() == expected.toString()

        where:
        field << createClass().getSerializableFields()
    }

    @Unroll("#featureName setting #field")
    def "serialize works correctly"() {
        when:
        def visitor = createClass()
        def node = new ParsedNode(null, visitor.getSerializedObjectName())
        def fieldValue = "value for ${field}"
        def expected = null
        if (field in ["applyToRollback", "applyToUpdate"]) {
            fieldValue = true
        } else if (field == "dbms") {
            fieldValue = "oracle, mock"
            expected = "[mock, oracle]"
        } else if (field == "contexts") {
            fieldValue = "test, prod"
            expected = "(prod), (test)"
        }
        if (expected == null) {
            expected = fieldValue
        }

        node.addChild(null, field, fieldValue)
        visitor.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        visitor.serialize().getChildValue(null, field).toString() == expected.toString()

        where:
        field << createClass().getSerializableFields()
    }

    @Unroll("#featureName: dbms='#dbms', context='#contexts', applyToRollback=#applyToRollback, applyToUpdate=#applyToUpdate")
    def "generateCheckSum the same regardless of dbms, context, applyToUpdate/Rollback"() {
        when:
        def blankVisitor = createClass()
        def visitor = createClass()
        visitor.setDbms(dbms)
        visitor.setContexts(contexts)
        visitor.setApplyToRollback(applyToRollback)
        visitor.setApplyToUpdate(applyToUpdate)

        then:
        blankVisitor.generateCheckSum() == visitor.generateCheckSum()

        where:
        [dbms, contexts, applyToRollback, applyToUpdate] << [
                [null, new HashSet<String>(), new HashSet<String>(["oracle"])],
                [null, new ContextExpression(), new ContextExpression("test")],
                [true, false],
                [true, false],
        ].combinations()


    }

    def ActionVisitor createClass() {
        Class.forName(getClass().getName().replaceAll('Test$', "")).newInstance()
    }
}
