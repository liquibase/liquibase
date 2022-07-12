package liquibase.diff.compare.core

import liquibase.database.core.PostgresDatabase
import liquibase.diff.Difference
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.core.ChangedColumnChangeGenerator
import liquibase.snapshot.SnapshotIdService
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Relation
import liquibase.structure.core.Table
import liquibase.util.StringUtil
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
