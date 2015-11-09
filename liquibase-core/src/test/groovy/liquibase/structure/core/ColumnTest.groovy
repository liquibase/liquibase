package liquibase.structure.core

import liquibase.structure.ObjectReference
import spock.lang.Specification
import spock.lang.Unroll

class ColumnTest extends Specification {

    @Unroll
    def "equals and hashCode works"() {
        expect:
        col1.equals(col2) == expected
        (col1.hashCode() == (col2 == null ? "" : col2.hashCode())) == expected

        where:
        col1                                             | col2                                                    | expected
        new Column("id").setRelation(new Table("table")) | new Column("id").setRelation(new Table("table"))        | true
        new Column("id").setRelation(new Table("table")) | new Column("other_col").setRelation(new Table("table")) | false
        new Column("id").setRelation(new Table("table")) | new Column("id").setRelation(new Table("TABLE"))        | false
        new Column("id").setRelation(new Table("table")) | new Column("ID").setRelation(new Table("TABLE"))        | false
        new Column("id").setRelation(new Table("table")) | null                                                    | false
        new Column("id").setRelation(new Table("table")) | "id"                                                    | false

    }

    @Unroll
    def "toString works"() {
        expect:
        col.toString() == expected

        where:
        col                                                                                                     | expected
        new Column("id")                                                                                        | "id"
        new Column("id").setRelation(new Table("test"))                                                         | "test.id"
        new Column("ID").setRelation(new Table("TEST"))                                                         | "TEST.ID"
        new Column("addr").setRelation(new Table(new ObjectReference("cat", "schema", "test")))                      | "cat.schema.test.addr"
        new Column("max(id)").setRelation(new Table(new ObjectReference("cat", "schema", "test"))).setComputed(true) | "max(id)"
    }
}
