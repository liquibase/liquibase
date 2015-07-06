package liquibase.structure.core

import liquibase.structure.ObjectName
import spock.lang.Specification
import spock.lang.Unroll

class UniqueConstraintTest extends Specification {

    def "constructor with columns"() {
        when:
        def uq = new UniqueConstraint(new ObjectName("schemaName", "testTable", "uq_name"), "col1", "col2")

        then:
        uq.name.toString() == "schemaName.testTable.uq_name"
        uq.columns*.toString() == ["schemaName.testTable.col1", "schemaName.testTable.col2"]
    }

    @Unroll
    def "equals/compareTo logic goes by the table if it is set, otherwise the UQ name"() {
        expect:
        uq1.equals(uq2) == expected

        if (expected) {
            assert uq1.compareTo(uq2) == 0
        } else {
            assert uq1.compareTo(uq2) != 0
        }

        where:
        uq1                                                        | uq2                                                         | expected
        new UniqueConstraint()                                           | new UniqueConstraint()                                            | true
        new UniqueConstraint()                                           | null                                                        | false
        new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1") | new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1")  | true
        new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1") | new UniqueConstraint(new ObjectName("tableName", "UQ2"), "col1")  | true //same table, but different names
        new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1") | new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col2")  | true //same name, but different columns
        new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1") | new UniqueConstraint(new ObjectName("otherTable", "UQ1"), "col1") | false //different table
        new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1") | new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")         | true //null table on one, same name
        new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")        | new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1")  | true //null table on one, same name
        new UniqueConstraint(new ObjectName(null, "UQ2"), "col1")        | new UniqueConstraint(new ObjectName("tableName", "UQ1"), "col1")  | false //null table on one, different name
        new UniqueConstraint(new ObjectName("tablename", "UQ2"), "col1") | new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")         | false //null table on one, different name
        new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")        | new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")         | true //null tables, same name
        new UniqueConstraint(new ObjectName(null, "UQ1"), "col1")        | new UniqueConstraint(new ObjectName(null, "UQ2"), "col1")         | false //null tables, different name
    }

}
