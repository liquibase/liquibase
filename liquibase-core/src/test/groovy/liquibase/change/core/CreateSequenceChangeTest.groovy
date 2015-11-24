package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.sdk.database.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.sqlgenerator.SqlGeneratorFactory
import spock.lang.Unroll

public class CreateSequenceChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new CreateSequenceChange();
        change.setSequenceName("SEQ_NAME");

        then:
        "Sequence SEQ_NAME created" == change.getConfirmationMessage()
    }

    def "checkWarnings"() {
        when:
        SqlGeneratorFactory.reset();
        def database = new MockDatabase()
        database.supportsSequences = false;

        def change = new CreateSequenceChange()
        change.sequenceName = "seq_my_table"

        then: "without errors"
        def errors = change.validate(database)
        assert !errors.hasErrors()

        def warnings = change.warn(database)
        assert warnings.hasWarnings()
        assert warnings.messages.size() == 1
        assert warnings.messages.get(0) == 'liquibase.statement.core.CreateSequenceStatement is not supported on mock, but createSequence will still execute'
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
        sequence.cacheSize = snapshotCacheSize
        sequence.willCycle = snapshotCycle

        def change = new CreateSequenceChange()
        change.sequenceName = sequence.name
        change.incrementBy = changeIncrementBy
        change.minValue = changeMinValue
        change.maxValue = changeMaxValue
        change.ordered = changeOrdered
        change.cacheSize = changeCacheSize
        change.cycle = changeCycle

        then: "sequence does not exist yet"
        assert change.checkStatus(database).status == ChangeStatus.Status.notApplied

        when: "sequence exists"
        snapshotFactory.addObjects(sequence)
        then:
        assert change.checkStatus(database).status == expectedResult

        where:
        changeIncrementBy | changeMaxValue | changeMinValue | changeOrdered | changeCacheSize | changeCycle | snapshotIncrementBy | snapshotMaxValue | snapshotMinValue | snapshotOrdered | snapshotCacheSize | snapshotCycle | expectedResult
        null | null | null | null  | null | null  | null | null | null | null  | null | null  | ChangeStatus.Status.complete
        2    | 4    | 6    | true  | 10   | true  | 2    | 4    | 6    | true  | 10   | true  | ChangeStatus.Status.complete
        2    | null | null | null  | null | null  | 2    | null | null | null  | null | null  | ChangeStatus.Status.complete
        null | 4    | null | null  | null | null  | null | 4    | null | null  | null  | null | ChangeStatus.Status.complete
        null | null | 6    | null  | null | null  | null | null | 6    | null  | null | null  | ChangeStatus.Status.complete
        null | null | null | true  | null | null  | null | null | null | true  | null | null  | ChangeStatus.Status.complete
        null | null | null | false | null | null  | null | null | null | false | null | null  | ChangeStatus.Status.complete
        null | null | null | null  | 10   | null  | null | null | null | null  | 10   | null  | ChangeStatus.Status.complete
        null | null | null | null  | null | true  | null | null | null | null  | null | true  | ChangeStatus.Status.complete
        null | null | null | null  | null | false | null | null | null | null  | null | false | ChangeStatus.Status.complete
        2    | null | null | null  | null | null  | 3    | null | null | null  | null | null  | ChangeStatus.Status.incorrect
        null | 4    | null | null  | null | null  | null | 5    | null | null  | null | null  | ChangeStatus.Status.incorrect
        null | null | 6    | null  | null | null  | 7    | null | null | null  | null | null  | ChangeStatus.Status.incorrect
        null | null | null | true  | null | null  | null | null | null | false | null | null  | ChangeStatus.Status.incorrect
        null | null | null | null  | 10   | null  | null | null | null | null  | 11   | null  | ChangeStatus.Status.incorrect
        null | null | null | null  | null | true  | null | null | null | null  | null | false | ChangeStatus.Status.incorrect
    }
}
