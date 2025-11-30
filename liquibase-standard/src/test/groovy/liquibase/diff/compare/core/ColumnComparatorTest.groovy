package liquibase.diff.compare.core

import liquibase.database.core.PostgresDatabase
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.snapshot.SnapshotIdService
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class ColumnComparatorTest extends Specification {

    @Unroll
    def "expect no differences between two columns that have essentially equivalent types (#type1 and #type2) for Postgres auto increment columns" (String type1, String type2) {
        when:
        def comparator = new ColumnComparator()
        def column1 = makeColumn(type1)
        def column2 = makeColumn(type2)

        then:
        def differences = comparator.findDifferences(column1, column2, new PostgresDatabase(), CompareControl.STANDARD, new DatabaseObjectComparatorChain(Collections.emptyList(), null), new HashSet<String>())
        differences.differences.isEmpty()

        where:
        type1 | type2
        // bigserial == int8
        "bigserial" | "int8"
        "int8" | "bigserial"
        "bigserial" | "bigserial"
        "int8" | "int8"
        // serial == int4
        "serial" | "int4"
        "int4" | "serial"
        "serial" | "serial"
        "int4" | "int4"
        // smallserial == int2
        "smallserial" | "int2"
        "int2" | "smallserial"
        "smallserial" | "smallserial"
        "int2" | "int2"
    }

    def "expect difference between two columns when the comment changes from '#before' to '#after'" (String before, String after) {
        when:
        def comparator = new ColumnComparator()
        def column1 = makeColumn("int8")
        def column2 = makeColumn("int8")
        column1.setRemarks(before)
        column2.setRemarks(after)

        then:
        def differences = comparator.findDifferences(column1, column2, new PostgresDatabase(), CompareControl.STANDARD, new DatabaseObjectComparatorChain(Collections.emptyList(), null), new HashSet<String>())
        !differences.differences.isEmpty()

        where:
        before | after
        null | "this column has a comment"
        null | ""
        "" | null
        "non-empty" | null
        "old comment" | "new comment"
    }

    def makeColumn(String type) {
        def column = new Column("colname")
        def dataType1 = new DataType(type)
        column.setType(dataType1)
        column.setRelation(new Table("catname", "schname", "tabname"))
        column.getRelation().setSnapshotId(SnapshotIdService.getInstance().generateId())
        column.setAutoIncrementInformation(new Column.AutoIncrementInformation())
        return column
    }
}
