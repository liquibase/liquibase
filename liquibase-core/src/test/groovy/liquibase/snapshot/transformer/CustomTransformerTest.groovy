package liquibase.snapshot.transformer

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.snapshot.Snapshot
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification

class CustomTransformerTest extends Specification {

    def "will transform correctly"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance)
                .add(new Table(new ObjectName("table1")))
                .add(new Table(new ObjectName("table2")))
                .add(new Column(new ObjectName("table1", "col1")))
                .add(new Column(new ObjectName("table1", "col2")))

        snapshot = snapshot.transform(new CustomTransformer<Column>(Column) {
            @Override
            Column transformObject(Column object, Scope scope) {
                object.name.name = object.name.name + "_updated"
                return object
            }
        }, JUnitScope.instance)

        then:
        snapshot.get(Column)*.name*.toString().sort() == ["table1.col1_updated", "table1.col2_updated"]
        snapshot.get(Table)*.name*.toString().sort() == ["table1", "table2"]

    }
}
