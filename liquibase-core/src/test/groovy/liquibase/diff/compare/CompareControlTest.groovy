package liquibase.diff.compare


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
}
