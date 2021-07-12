package liquibase.structure.core

import liquibase.parser.core.ParsedNode
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

class IndexTest extends Specification {

    @Unroll
    def "toString test"() {
        expect:
        index.toString() == expected

        where:
        index                                                                                     | expected
        new Index()                                                                               | "(unnamed index)"
        new Index("idx_name")                                                                     | "idx_name"
        new Index("idx_name", null, null, "tab_name", new Column("col1"), new Column("col2"))     | "idx_name ON tab_name(col1, col2)"
        new Index("idx_name", null, "schem", "tab_name", new Column("col1"), new Column("col2"))  | "idx_name ON schem.tab_name(col1, col2)"
        new Index("idx_name", "cat", "schem", "tab_name", new Column("col1"), new Column("col2")) | "idx_name ON schem.tab_name(col1, col2)"
    }

    def "equals and hasCode"() {
        expect:
        index1.equals(index2) == expected
        if (expected) {
            assert index1.hashCode() == index2.hashCode()
        }

        where:
        index1                                                                                     | index2                                                                                     | expected
        new Index()                                                                                | new Index()                                                                                | true
        new Index("idx_name")                                                                      | new Index("idx_name")                                                                      | true
        new Index("idx_name", null, null, "tab_name", new Column("col1"), new Column("col2"))      | new Index("idx_name", null, null, "tab_name", new Column("col1"), new Column("col2"))      | true
        new Index("idx_name", null, "schem", "tab_name", new Column("col1"), new Column("col2"))   | new Index("idx_name", null, "schem", "tab_name", new Column("col1"), new Column("col2"))   | true
        new Index("idx_name", "cat", "schem", "tab_name", new Column("col1"), new Column("col2"))  | new Index("idx_name", "cat", "schem", "tab_name", new Column("col1"), new Column("col2"))  | true
        new Index("idx_name", null, "schema1", "tab_name", new Column("col1"), new Column("col2")) | new Index("idx_name", null, "schema2", "tab_name", new Column("col1"), new Column("col2")) | false
    }

    def "load"() {
        when:
        def node = new ParsedNode(null, "index")
        node.addChildren([
                name      : "idx_test_table",
                snapshotId: "123",
                table     : "liquibase.structure.core.Table#5874102",
                unique    : false,
                columns   : [
                        new ParsedNode(null, "column").addChildren([
                                computed: true,
                                descending: false,
                                name: "lower(name)::text",
                                snapshotId: "183313",
                        ]),
                        new ParsedNode(null, "column").addChildren([
                                computed: true,
                                descending: false,
                                name: "lower(address)::text",
                                snapshotId: "663234",
                        ]),

                ],
        ])

        def index = new Index()
        index.load(node, new JUnitResourceAccessor())

        then:
        index.name == "idx_test_table"
        !index.unique
        index.columns.size() == 2
    }
}
