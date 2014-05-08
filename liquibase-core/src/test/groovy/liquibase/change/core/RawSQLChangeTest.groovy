package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.parser.core.ParsedNode
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory

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

    def "load with sql as value or as 'sql' child"() {
        when:
        def changeFromValue = new RawSQLChange()
        changeFromValue.load(new ParsedNode(null, "sql").setValue("select * from x"), resourceSupplier.simpleResourceAccessor)

        def changeFromChild = new RawSQLChange()
        changeFromChild.load(new ParsedNode(null, "sql").addChild(null, "sql", "select * from y"), resourceSupplier.simpleResourceAccessor)

        then:
        changeFromValue.getSql() == "select * from x"
        changeFromChild.getSql() == "select * from y"

    }

}