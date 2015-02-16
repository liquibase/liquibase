package liquibase.snapshot.core

import liquibase.JUnitScope
import liquibase.snapshot.Snapshot
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.View
import spock.lang.Specification

class ColumnRelateLogicTest extends Specification {

    def "combines columns and tables and views"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance)
                .add(new Table("table1"))
                .add(new Table("table2"))
                .add(new Table("TABLE1"))
                .add(new Table(new ObjectName("other_schema", "table1")))
                .add(new Table(new ObjectName("other_catalog", "other_schema", "table1")))

                .add(new View("view1"))
                .add(new View("view2"))
                .add(new View("VIEW1"))
                .add(new View(new ObjectName("other_schema", "view1")))
                .add(new View(new ObjectName("other_catalog", "other_schema", "view1")))

                .add(new Column("id_table1").setRelation(new Table("table1")))
                .add(new Column("name_table1").setRelation(new Table("table1")))
                .add(new Column("ID_TABLE1").setRelation(new Table("TABLE1")))
                .add(new Column("id_table2").setRelation(new Table("table2")))
                .add(new Column("name_table2").setRelation(new Table("table2")))
                .add(new Column("address_table2").setRelation(new Table("table2")))
                .add(new Column("id_other_schema.table1").setRelation(new Table(new ObjectName("other_schema", "table1"))))
                .add(new Column("id_other_catalog.other_schema.table1").setRelation(new Table(new ObjectName("other_catalog", "other_schema", "table1"))))

                .add(new Column("id_view1").setRelation(new View("view1")))
                .add(new Column("address_view1").setRelation(new View("view1")))

                .add(new Column("id_view2").setRelation(new View("view2")))
                .add(new Column("age_view2").setRelation(new View("view2")))

                .add(new Column("ID_VIEW1").setRelation(new View("VIEW1")))

                .add(new Column("id_other_schema.view1").setRelation(new View(new ObjectName("other_schema", "view1"))))
                .add(new Column("id_other_catalog.other_schema.view1").setRelation(new View(new ObjectName("other_catalog", "other_schema", "view1"))))

        new ColumnRelateLogic().relate(snapshot)

        then:
        for (Column column : snapshot.get(Column)) {
            assert column.getRelation().getSnapshotId() != null
            assert column.getSimpleName().endsWith(column.getRelation().getSimpleName())
        }

        for (Table table : snapshot.get(Table)) {
            table.columns.size() > 0
            for (Column column : table.columns) {
                assert column.getSnapshotId() != null
                assert column.getSimpleName().endsWith(table.getSimpleName())
            }
        }

        for (View view : snapshot.get(View)) {
            view.columns.size() > 0
            for (Column column : view.columns) {
                assert column.getSnapshotId() != null
                assert column.getSimpleName().endsWith(view.getSimpleName())
            }
        }

    }
}
