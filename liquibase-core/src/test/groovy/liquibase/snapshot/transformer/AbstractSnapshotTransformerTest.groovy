package liquibase.snapshot.transformer

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.snapshot.Snapshot
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import spock.lang.Specification

class AbstractSnapshotTransformerTest extends Specification {

    def "only transforms objects that match the generic interface"() {
        when:
        def transformer = new AbstractSnapshotTransformer<Column>(Column) {

            @Override
            Column transformObject(Column object, Scope scope) {
                object.type = new DataType("int");
                return object;
            }
        }

        def snapshot = new Snapshot(JUnitScope.instance)
                .add(new Table(new ObjectName("table1")))
                .add(new Table(new ObjectName("table2")))
                .add(new Column(Table, new ObjectName("table1"), "col1"))
                .add(new Column(Table, new ObjectName("table1"), "col2"))

        then:
        snapshot.transform(transformer, JUnitScope.instance).get(Column)*.type*.toString() == ["int", "int"]

    }
}
