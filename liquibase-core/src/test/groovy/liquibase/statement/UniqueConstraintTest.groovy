package liquibase.statement

import liquibase.AbstractExtensibleObjectTest

class UniqueConstraintTest extends AbstractExtensibleObjectTest {

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("columns")
        return properties
    }

    def "get/add columns"() {
        expect:
        def constraint = new UniqueConstraint()
        constraint.getColumns() == []

        constraint.addColumns("a")
        constraint.getColumns() == ["a"]

        constraint.addColumns("b", "c")
        constraint.getColumns() == ["a", "b", "c"]

        constraint.addColumns()
        constraint.getColumns() == ["a", "b", "c"]

        constraint.addColumns(null)
        constraint.getColumns() == ["a", "b", "c"]
    }

}
