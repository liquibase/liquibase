package liquibase.structure.core

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
}
