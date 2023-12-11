package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory

public class DropSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new DropSequenceChange();
        change.setSequenceName("SEQ_NAME");

        then:
        "Sequence SEQ_NAME dropped" == change.getConfirmationMessage()
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def sequence = new liquibase.structure.core.Sequence(null, null, "seq_test")

        def change = new DropSequenceChange()
        change.sequenceName = sequence.name

        then: "sequence is not there yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.complete

        when: "sequence is there"
        snapshotFactory.addObjects(sequence)
        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied
    }
}