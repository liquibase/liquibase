package liquibase.snapshot

import spock.lang.Specification

class SnapshotIdServiceTest extends Specification {

    def generate() {
        when:
        def id1 = SnapshotIdService.instance.generateId()
        def id2 = SnapshotIdService.instance.generateId()

        then:
        id1 != id2
        id1.subSequence(0, 4) == id2.subSequence(0, 4)
    }
}
