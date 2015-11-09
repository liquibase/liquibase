package liquibase.snapshot

import liquibase.JUnitScope
import liquibase.structure.ObjectReference
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class SnapshotTest extends Specification {

    def "adding an object sets a snapshot id once and only once"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance)
        def obj1 = new Table(new ObjectReference("test_table1"))
        def obj2 = new Table(new ObjectReference("test_table2"))

        then:
        obj1.getSnapshotId() == null
        obj2.getSnapshotId() == null

        when:
        snapshot.add(obj1)

        then:
        obj1.getSnapshotId() != null
        obj2.getSnapshotId() == null

        when: "Adding an object again"
        def obj1Id = obj1.getSnapshotId()
        snapshot.add(obj1)

        then:
        obj1Id == obj1.getSnapshotId()

        when:
        snapshot.add(obj2)

        then:
        obj2.getSnapshotId() != null
        obj1.getSnapshotId() != obj2.getSnapshotId()

    }

    @Unroll
    def "getAll finds objects by partial name correctly"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance).addAll([
                new Table("cat1", "schema-a", "table-a-1"),
                new Table("cat1", "schema-a", "table-a-2"),
                new Table("cat1", "schema-b", "table-b-1"),
                new Column(new ObjectReference("cat1", "schema-a", "table-a-1", "col-a-1-x")),
                new Column(new ObjectReference("cat1", "schema-a", "table-a-1", "col2")),
                new Column(new ObjectReference("cat1", "schema-a", "table-a-2", "col-a-2-x")),
                new Column(new ObjectReference("cat1", "schema-a", "table-a-2", "col2")),
                new Column(new ObjectReference("cat1", "schema-b", "table-b-1", "col-b-1-x")),
        ])

        then:
        snapshot.getAll(type, name)*.toString() == expected

        where:
        type   | name                                                         | expected
        Column | new ObjectReference("cat1", "schema-a", "table-a-1", "col-a-1-x") | ["cat1.schema-a.table-a-1.col-a-1-x"]
    }
}
