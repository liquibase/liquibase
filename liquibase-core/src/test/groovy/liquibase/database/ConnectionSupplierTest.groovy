package liquibase.database

import liquibase.Scope
import liquibase.database.core.UnsupportedDatabaseSupplier
import liquibase.sdk.database.MockDatabase
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that


class ConnectionSupplierTest extends Specification {

    @Unroll("#featureName (#depth, #includePartials, #includeNulls)")
    def "getContainers"() {
        when:
        def supplier = new UnsupportedDatabaseSupplier() {
            @Override
            Database getDatabase(Scope scope) {
                return new MockDatabase().setMaxReferenceContainerDepth(1);
            }
        }

        then:
        that supplier.getContainers(depth, includePartials, includeNulls)*.toString(), containsInAnyOrder(expected.toArray())

        where:
        depth | includePartials | includeNulls | expected
        1     | false            | false        | ["lbschema", "lbschema2"]
        2     | false            | false        | ["lbcat.lbschema", "lbcat.lbschema2","lbcat2.lbschema", "lbcat2.lbschema2"]
        1     | true            | false        | ["lbschema", "lbschema2"]
        2     | true            | false         | ["lbcat.lbschema", "lbcat.lbschema2","lbcat2.lbschema", "lbcat2.lbschema2", "lbschema", "lbschema2"]
        1     | true            | true         | ["lbschema", "lbschema2", "#UNSET"]
        2     | true            | true         | ["lbcat.lbschema", "lbcat.lbschema2","lbcat2.lbschema", "lbcat2.lbschema2", "lbschema", "lbschema2", "lbcat.#UNSET", "lbcat2.#UNSET", "#UNSET.lbschema", "#UNSET.lbschema2", "#UNSET.#UNSET", "#UNSET"]
    }

    def "getReferenceContainers: depth 2"() {
        expect:
        def supplier = new UnsupportedDatabaseSupplier() {
            @Override
            Database getDatabase(Scope scope) {
                return new MockDatabase().setMaxReferenceContainerDepth(2);
            }
        }

        that supplier.getReferenceContainers(true)*.toString(), containsInAnyOrder([
                "lbschema",
                "lbschema2",
                "#UNSET",
                "lbcat.lbschema",
                "lbcat2.lbschema",
                "lbcat.lbschema2",
                "lbcat2.lbschema2",
                "#UNSET.lbschema",
                "#UNSET.lbschema2",
                "#UNSET.#UNSET",
        ].toArray())

        that supplier.getReferenceContainers(false)*.toString(), containsInAnyOrder([
                "lbcat.lbschema",
                "lbcat.lbschema2",
                "lbcat2.lbschema",
                "lbcat2.lbschema2",
        ].toArray())
    }

    def "getObjectNames"() {
        expect:
        def supplier = new UnsupportedDatabaseSupplier()

        that supplier.getObjectNames(Table.class, true)*.toString(), containsInAnyOrder([
                "lbschema.test_table",
                "#UNSET.test_table",
                "lbschema2.test_table",
                "lbschema.TEST_TABLE",
                "#UNSET.TEST_TABLE",
                "lbschema2.TEST_TABLE",
                "lbschema.TestTable",
                "#UNSET.TestTable",
                "lbschema2.TestTable"].toArray())
    }
}
