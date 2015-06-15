package liquibase.snapshot.transformer

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.snapshot.Snapshot
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification

class TransformerListTest extends Specification {

    def "will transform correctly"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance)
                .add(new Table(new ObjectName("table1")))
                .add(new Table(new ObjectName("table2")))
                .add(new Column(new ObjectName("table1", "col1")))
                .add(new Column(new ObjectName("table1", "col2")))

        snapshot = snapshot.transform(new TransformerList(new CustomTransformer<DatabaseObject>(DatabaseObject) {
            @Override
            DatabaseObject transformObject(DatabaseObject object, Scope scope) {
                object.name.name = object.name.name + "_first"
                return object
            }
        }, new CustomTransformer<DatabaseObject>(DatabaseObject) {
            @Override
            DatabaseObject transformObject(DatabaseObject object, Scope scope) {
                object.name.name = object.name.name + "_second"
                return object
            }
        }), JUnitScope.instance)

        then:
        snapshot.get(Column)*.name*.toString().sort() == ["table1.col1_first_second", "table1.col2_first_second"]
        snapshot.get(Table)*.name*.toString().sort() == ["table1_first_second", "table2_first_second"]

    }

}
