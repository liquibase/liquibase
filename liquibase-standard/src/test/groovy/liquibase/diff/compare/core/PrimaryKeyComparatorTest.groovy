package liquibase.diff.compare.core

import liquibase.database.Database
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class PrimaryKeyComparatorTest extends Specification {

    def comparator = new PrimaryKeyComparator()

    private PrimaryKey createPrimaryKey(String pkName, String tableName, List<String> columnNames = []) {
        PrimaryKey pk = new PrimaryKey()
        pk.setName(pkName)
        if (tableName != null) {
            Table table = new Table()
            table.setName(tableName)
            table.setSchema(new Schema("catalog", "schema"))
            pk.setTable(table)
        }
        columnNames.eachWithIndex { columnName, i ->
            pk.addColumn(i, new Column(columnName))
        }
        return pk
    }

    private boolean isSame(PrimaryKey pk1, PrimaryKey pk2) {
        def database = Mock(Database) {
            isCaseSensitive() >> false
        }
        def chain = Mock(DatabaseObjectComparatorChain) {
            getSchemaComparisons() >> new CompareControl.SchemaComparison[0]
        }
        return comparator.isSameObject(pk1, pk2, database, chain)
    }

    @Unroll
    def "isSameObject with pk1=#pk1Name on #table1Name and pk2=#pk2Name on #table2Name should be #expected"() {
        expect:
        isSame(createPrimaryKey(pk1Name, table1Name), createPrimaryKey(pk2Name, table2Name)) == expected

        where:
        pk1Name           | table1Name | pk2Name           | table2Name | expected
        // Same PK name, no table - should match
        "PK_FOO"          | null       | "PK_FOO"          | null       | true
        // Different PK name, no table - should not match
        "PK_FOO"          | null       | "PK_BAR"          | null       | false
        // Same PK name, same table - should match
        "PK_FOO"          | "TABLE_A"  | "PK_FOO"          | "TABLE_A"  | true
        // Different PK name, same table, no columns on either side - should NOT match (this was the bug)
        "PK_FOO"          | "TABLE_A"  | "PK_BAR"          | "TABLE_A"  | false
        // Same PK name, different table - should not match
        "PK_FOO"          | "TABLE_A"  | "PK_FOO"          | "TABLE_B"  | false
        // Different PK name, different table - should not match
        "PK_FOO"          | "TABLE_A"  | "PK_BAR"          | "TABLE_B"  | false
        // One PK has null name, same table - should match (wildcard behavior)
        null              | "TABLE_A"  | "PK_FOO"          | "TABLE_A"  | true
        // Reverse direction: null name on the other side - should also match
        "PK_FOO"          | "TABLE_A"  | null              | "TABLE_A"  | true
        // Both PKs have null name, same table - should match
        null              | "TABLE_A"  | null              | "TABLE_A"  | true
    }

    @Unroll
    def "isSameObject with columns: pk1=#pk1Name#pk1Columns vs pk2=#pk2Name#pk2Columns on same table should be #expected"() {
        expect:
        isSame(createPrimaryKey(pk1Name, "TABLE_A", pk1Columns), createPrimaryKey(pk2Name, "TABLE_A", pk2Columns)) == expected

        where:
        pk1Name    | pk1Columns    | pk2Name    | pk2Columns          | expected
        // Snapshot-vs-snapshot: auto-generated names differ across environments (e.g. Oracle SYS_C...),
        // but a table has a single PK, so two snapshotted PKs on the same table are the same object
        "SYS_C001" | ["ID"]        | "SYS_C002" | ["ID"]              | true
        // Same, even when columns differ - findDifferences() reports the column change
        "SYS_C001" | ["ID"]        | "SYS_C002" | ["ID", "VERSION"]   | true
        // Name-based lookup (e.g. primaryKeyExists precondition example has no columns):
        // a name mismatch means the requested PK does not exist
        "PK_FOO"   | []            | "SYS_C001" | ["ID"]              | false
        // Reverse direction: snapshot PK vs column-less example
        "SYS_C001" | ["ID"]        | "PK_FOO"   | []                  | false
        // Matching names always match, regardless of columns
        "PK_FOO"   | ["ID"]        | "pk_foo"   | ["ID", "VERSION"]   | true
        // Wildcard: null-named example with no columns matches any PK on the table
        null       | []            | "SYS_C001" | ["ID"]              | true
    }
}
