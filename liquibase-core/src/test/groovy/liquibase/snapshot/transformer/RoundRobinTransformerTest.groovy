package liquibase.snapshot.transformer

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.snapshot.Snapshot
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectReference
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification

class RoundRobinTransformerTest extends Specification {

    def snapshot;

    def setup() {
        snapshot = new Snapshot(JUnitScope.instance)
                .add(new Table(new ObjectReference("table1")))
                .add(new Table(new ObjectReference("table2")))
                .add(new Table(new ObjectReference("table3")))
                .add(new Column(new ObjectReference("table1", "col1")))
                .add(new Column(new ObjectReference("table1", "col2")))
                .add(new Column(new ObjectReference("table1", "col3")))
                .add(new Column(new ObjectReference("table2", "col1")))
                .add(new Column(new ObjectReference("table2", "col2")))
                .add(new Column(new ObjectReference("table2", "col3")))
                .add(new Column(new ObjectReference("table3", "col1")))
                .add(new Column(new ObjectReference("table3", "col2")))
                .add(new Column(new ObjectReference("table3", "col3")))
    }

    def "will transform specific type with two transformers"() {
        when:
        snapshot = snapshot.transform(new RoundRobinTransformer([Column] as Class[], new CustomTransformer<DatabaseObject>(DatabaseObject) {
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
        snapshot.get(Column)*.name*.toString().find({ it.endsWith("_first") }).size() > 1
        snapshot.get(Column)*.name*.toString().find({ it.endsWith("_second") }).size() > 1

        snapshot.get(Table)*.name*.toString().sort() == ["table1", "table2", "table3"]

    }

    def "will transform all types with two transformers"() {
        when:
        snapshot = snapshot.transform(new RoundRobinTransformer(new CustomTransformer<DatabaseObject>(DatabaseObject) {
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
        snapshot.get(Column)*.name*.toString().find({ it.endsWith("_first") }).size() > 1
        snapshot.get(Column)*.name*.toString().find({ it.endsWith("_second") }).size() > 1

        snapshot.get(Table)*.name*.toString().find({ it.endsWith("_first") }).size() > 1
        snapshot.get(Table)*.name*.toString().find({ it.endsWith("_second") }).size() > 1
    }

    def "will return original with empty transformers"() {
        when:
        snapshot = snapshot.transform(new RoundRobinTransformer(), JUnitScope.instance)

        then:
        snapshot.get(Column)*.name*.toString().sort() == ["table1.col1",
                                                          "table1.col2",
                                                          "table1.col3",
                                                          "table2.col1",
                                                          "table2.col2",
                                                          "table2.col3",
                                                          "table3.col1",
                                                          "table3.col2",
                                                          "table3.col3"]
        snapshot.get(Table)*.name*.toString().sort() == ["table1", "table2", "table3"]

    }
}