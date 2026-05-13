package liquibase.diff.compare.core

import liquibase.database.Database
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class PrimaryKeyComparatorTest extends Specification {

    def comparator = new PrimaryKeyComparator()

    private PrimaryKey createPrimaryKey(String pkName, String tableName) {
        PrimaryKey pk = new PrimaryKey()
        pk.setName(pkName)
        if (tableName != null) {
            Table table = new Table()
            table.setName(tableName)
            table.setSchema(new Schema("catalog", "schema"))
            pk.setTable(table)
        }
        return pk
    }

    @Unroll
    def "isSameObject with pk1=#pk1Name on #table1Name and pk2=#pk2Name on #table2Name should be #expected"() {
        given:
        def pk1 = createPrimaryKey(pk1Name, table1Name)
        def pk2 = createPrimaryKey(pk2Name, table2Name)
        def database = Mock(Database) {
            isCaseSensitive() >> false
        }
        def chain = Mock(DatabaseObjectComparatorChain) {
            getSchemaComparisons() >> new CompareControl.SchemaComparison[0]
        }

        expect:
        comparator.isSameObject(pk1, pk2, database, chain) == expected

        where:
        pk1Name           | table1Name | pk2Name           | table2Name | expected
        // Same PK name, no table - should match
        "PK_FOO"          | null       | "PK_FOO"          | null       | true
        // Different PK name, no table - should not match
        "PK_FOO"          | null       | "PK_BAR"          | null       | false
        // Same PK name, same table - should match
        "PK_FOO"          | "TABLE_A"  | "PK_FOO"          | "TABLE_A"  | true
        // Different PK name, same table - should NOT match (this was the bug)
        "PK_FOO"          | "TABLE_A"  | "PK_BAR"          | "TABLE_A"  | false
        // Same PK name, different table - should not match
        "PK_FOO"          | "TABLE_A"  | "PK_FOO"          | "TABLE_B"  | false
        // Different PK name, different table - should not match
        "PK_FOO"          | "TABLE_A"  | "PK_BAR"          | "TABLE_B"  | false
        // One PK has null name, same table - should match (wildcard behavior)
        null               | "TABLE_A"  | "PK_FOO"          | "TABLE_A"  | true
        // Both PKs have null name, same table - should match
        null               | "TABLE_A"  | null               | "TABLE_A"  | true
    }
}
