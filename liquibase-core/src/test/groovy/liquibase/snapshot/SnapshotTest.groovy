package liquibase.snapshot

import liquibase.JUnitScope
import liquibase.structure.core.Table
import spock.lang.Specification

class SnapshotTest extends Specification {

    def "adding an object sets a snapshot id once and only once"() {
        when:
        def snapshot = new Snapshot(JUnitScope.instance)
        def obj1 = new Table("test_table1")
        def obj2 = new Table("test_table2")

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
}
