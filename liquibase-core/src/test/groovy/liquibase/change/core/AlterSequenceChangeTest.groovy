package liquibase.change.core

import liquibase.action.ActionStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
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
        assert change.checkStatus(database).status == ActionStatus.Status.unknown

        when: "sequence exists"
        snapshotFactory.addObjects(sequence)
        then:
        assert change.checkStatus(database).status == expectedResult

        where:
        changeIncrementBy | changeMaxValue | changeMinValue | changeOrdered | snapshotIncrementBy | snapshotMaxValue | snapshotMinValue | snapshotOrdered | expectedResult
        null | null | null | null  | null | null | null | null  | ActionStatus.Status.unknown
        2    | 4    | 6    | true  | 2    | 4    | 6    | true  | ActionStatus.Status.applied
        2    | null | null | null  | 2    | null | null | null  | ActionStatus.Status.applied
        null | 4    | null | true  | null | 4    | null | true  | ActionStatus.Status.applied
        null | null | 6    | null  | null | null | 6    | null  | ActionStatus.Status.applied
        null | null | null | true  | null | null | null | true  | ActionStatus.Status.applied
        null | null | null | false | null | null | null | false | ActionStatus.Status.applied
        2    | null | null | null  | 3    | null | null | null  | ActionStatus.Status.incorrect
        null | 4    | null | null  | null | 5    | null | null  | ActionStatus.Status.incorrect
        null | null | 6    | null  | null | null | 7    | null  | ActionStatus.Status.incorrect
        null | null | null | true  | null | null | null | false | ActionStatus.Status.incorrect
    }
}
