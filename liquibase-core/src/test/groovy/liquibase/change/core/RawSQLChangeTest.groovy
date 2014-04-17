package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RawSQLChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new RawSQLChange()

        then:
        "Custom SQL executed" == change.getConfirmationMessage()
    }

    @Override
    protected String getExpectedChangeName() {
        return "sql"
    }

    @Override
    protected boolean canUseStandardGenerateCheckSumTest() {
        return false;
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def change = new RawSQLChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check raw sql status"
    }

}