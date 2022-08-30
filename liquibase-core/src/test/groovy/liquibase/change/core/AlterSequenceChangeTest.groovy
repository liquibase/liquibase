package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import spock.lang.Unroll

public class AlterSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        then:
        refactoring.getConfirmationMessage() == "Sequence SEQ_NAME altered"
    }

    @Unroll
    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def sequence = new liquibase.structure.core.Sequence(null, null, "seq_test")
        sequence.incrementBy = snapshotIncrementBy
        sequence.minValue = snapshotMinValue
        sequence.maxValue = snapshotMaxValue
        sequence.ordered = snapshotOrdered

        def change = new AlterSequenceChange()
        change.sequenceName = sequence.name
        change.incrementBy = changeIncrementBy
        change.minValue = changeMinValue
        change.maxValue = changeMaxValue
        change.ordered = changeOrdered

        then: "sequence does not exist yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown

        when: "sequence exists"
        snapshotFactory.addObjects(sequence)
        then:
        assert change.checkStatus(database).status == expectedResult

        where:
        changeIncrementBy | changeMaxValue | changeMinValue | changeOrdered | snapshotIncrementBy | snapshotMaxValue | snapshotMinValue | snapshotOrdered | expectedResult
        null | null | null | null  | null | null | null | null  | ChangeStatus.Status.unknown
        2    | 4    | 6    | true  | 2    | 4    | 6    | true  | ChangeStatus.Status.complete
        2    | null | null | null  | 2    | null | null | null  | ChangeStatus.Status.complete
        null | 4    | null | true  | null | 4    | null | true  | ChangeStatus.Status.complete
        null | null | 6    | null  | null | null | 6    | null  | ChangeStatus.Status.complete
        null | null | null | true  | null | null | null | true  | ChangeStatus.Status.complete
        null | null | null | false | null | null | null | false | ChangeStatus.Status.complete
        2    | null | null | null  | 3    | null | null | null  | ChangeStatus.Status.incorrect
        null | 4    | null | null  | null | 5    | null | null  | ChangeStatus.Status.incorrect
        null | null | 6    | null  | null | null | 7    | null  | ChangeStatus.Status.incorrect
        null | null | null | true  | null | null | null | false | ChangeStatus.Status.incorrect
    }
}
