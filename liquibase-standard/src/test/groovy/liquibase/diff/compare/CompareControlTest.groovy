package liquibase.diff.compare


import liquibase.structure.core.DatabaseObjectFactory
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification

class CompareControlTest extends Specification {

    def "CompareControl correctly splits schemas"() {
        given:
        String[] schemas = ["aaa.bbb", "ccc.ddd"]
        def result = new CompareControl(schemas, null)

        expect:
        result.getSchemaComparisons()[0].getReferenceSchema().getCatalogName() == "aaa"
        result.getSchemaComparisons()[0].getReferenceSchema().getSchemaName() == "bbb"
        result.getSchemaComparisons()[0].getComparisonSchema().getCatalogName() == "ccc"
        result.getSchemaComparisons()[0].getComparisonSchema().getSchemaName() == "ddd"

    }

    def "narrowing the compared types leaves the standard types and the caller's set untouched"() {
        given:
        def standardTypes = DatabaseObjectFactory.getInstance().getStandardTypes()
        def standardTypesBefore = new HashSet(standardTypes)
        def callerTypes = new HashSet([Table, Schema])

        when: "a holder narrows the control's compared types in place"
        new CompareControl().getComparedTypes().retainAll([Table])
        new CompareControl(callerTypes).getComparedTypes().retainAll([Table])

        then:
        standardTypes == standardTypesBefore
        new CompareControl().getComparedTypes().contains(Schema)
        callerTypes == [Table, Schema] as Set

        cleanup:
        standardTypes.addAll(standardTypesBefore)
    }
}
