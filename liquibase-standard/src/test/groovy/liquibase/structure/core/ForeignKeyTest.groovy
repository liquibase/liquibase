package liquibase.structure.core

import spock.lang.Specification
import spock.lang.Unroll

class ForeignKeyTest extends Specification {

    def static baseCol1 = new Column(Table, null, null, "fk_table", "base_col1")
    def static baseCol2 = new Column(Table, null, null, "fk_table", "base_col2")

    def static pkTable = new Table(null, null, "pk_table")
    def static pkCol1 = new Column(Table, null, null, "pk_table", "pk_col1")
    def static pkCol2 = new Column(Table, null, null, "pk_table", "pk_col2")

    @Unroll
    def "toString() logic"() {
        expect:
        fk.toString() == expected

        where:
        fk                                                                                                                                       | expected
        new ForeignKey("fk_name")                                                                                                                | "fk_name(null[] -> null[])"
        new ForeignKey("fk_name", null, null, "fk_table", baseCol1).setPrimaryKeyTable(pkTable).addPrimaryKeyColumn(pkCol1)                      | "fk_name(fk_table[base_col1] -> pk_table[pk_col1])"
        new ForeignKey("fk_name", null, null, "fk_table", baseCol1, baseCol2).setPrimaryKeyTable(pkTable).setPrimaryKeyColumns([pkCol1, pkCol2]) | "fk_name(fk_table[base_col1, base_col2] -> pk_table[pk_col1, pk_col2])"
    }
}
