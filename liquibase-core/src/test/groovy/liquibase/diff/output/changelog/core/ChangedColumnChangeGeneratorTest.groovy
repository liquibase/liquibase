package liquibase.diff.output.changelog.core

import liquibase.database.core.PostgresDatabase
import liquibase.diff.Difference
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.snapshot.SnapshotIdService
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Relation
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class ChangedColumnChangeGeneratorTest extends Specification {

    @Unroll
    def "expect no differences between two columns that have essentially equivalent types (#type1 and #type2) for auto increment columns" (String type1, String type2) {
        when:
        def generator = new ChangedColumnChangeGenerator()
        def columnName = "colname"
        def column = new Column(columnName)
        def dataType1 =new DataType(type1)
        column.setType(dataType1)
        column.setRelation(new Table("catname", "schname", "tabname"))
        column.getRelation().setSnapshotId(SnapshotIdService.getInstance().generateId())
        column.setAutoIncrementInformation(new Column.AutoIncrementInformation())
        def differences = new ObjectDifferences(CompareControl.STANDARD)
        differences.addDifference("type", dataType1, new DataType(type2))


        then:
        generator.fixChanged(column, differences, new DiffOutputControl(), new PostgresDatabase(), new PostgresDatabase(), null).size() == 0

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
}
