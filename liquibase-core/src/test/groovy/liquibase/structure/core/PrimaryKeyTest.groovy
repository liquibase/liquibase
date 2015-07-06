package liquibase.structure.core

import liquibase.structure.ObjectName
import spock.lang.Specification
import spock.lang.Unroll

public class PrimaryKeyTest extends Specification {

    def "constructor with columns"() {
        when:
        def pk = new PrimaryKey(new ObjectName("schemaName", "testTable", "pk_name"), "col1", "col2")

        then:
        pk.name.toString() == "schemaName.testTable.pk_name"
        pk.columns*.toString() == ["schemaName.testTable.col1", "schemaName.testTable.col2"]
    }

    @Unroll
    def "equals/compareTo logic goes by the table if it is set, otherwise the PK name"() {
        expect:
        pk1.equals(pk2) == expected

        if (expected) {
            assert pk1.compareTo(pk2) == 0
        } else {
            assert pk1.compareTo(pk2) != 0
        }

        where:
        pk1                                                        | pk2                                                         | expected
        new PrimaryKey()                                           | new PrimaryKey()                                            | true
        new PrimaryKey()                                           | null                                                        | false
        new PrimaryKey(new ObjectName("tableName", "PK1"), "col1") | new PrimaryKey(new ObjectName("tableName", "PK1"), "col1")  | true
        new PrimaryKey(new ObjectName("tableName", "PK1"), "col1") | new PrimaryKey(new ObjectName("tableName", "PK2"), "col1")  | true //same table, but different names
        new PrimaryKey(new ObjectName("tableName", "PK1"), "col1") | new PrimaryKey(new ObjectName("tableName", "PK1"), "col2")  | true //same name, but different columns
        new PrimaryKey(new ObjectName("tableName", "PK1"), "col1") | new PrimaryKey(new ObjectName("otherTable", "PK1"), "col1") | false //different table
        new PrimaryKey(new ObjectName("tableName", "PK1"), "col1") | new PrimaryKey(new ObjectName(null, "PK1"), "col1")         | true //null table on one, same name
        new PrimaryKey(new ObjectName(null, "PK1"), "col1")        | new PrimaryKey(new ObjectName("tableName", "PK1"), "col1")  | true //null table on one, same name
        new PrimaryKey(new ObjectName(null, "PK2"), "col1")        | new PrimaryKey(new ObjectName("tableName", "PK1"), "col1")  | false //null table on one, different name
        new PrimaryKey(new ObjectName("tablename", "PK2"), "col1") | new PrimaryKey(new ObjectName(null, "PK1"), "col1")         | false //null table on one, different name
        new PrimaryKey(new ObjectName(null, "PK1"), "col1")        | new PrimaryKey(new ObjectName(null, "PK1"), "col1")         | true //null tables, same name
        new PrimaryKey(new ObjectName(null, "PK1"), "col1")        | new PrimaryKey(new ObjectName(null, "PK2"), "col1")         | false //null tables, different name
    }
}
