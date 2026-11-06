package liquibase.change.core

import liquibase.change.ChangeStatus;
import liquibase.change.StandardChangeTest
import liquibase.database.core.Db2zDatabase
import liquibase.database.core.MockDatabase
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNodeException
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.core.RawCompoundStatement
import liquibase.statement.core.RawSqlStatement

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

    def "a Db2 for z/OS CALL is a compound statement even when comments come first"() {
        when:
        def change = new RawSQLChange()
        change.setSql(sql)
        change.setStripComments(false)
        def statements = change.generateStatements(new Db2zDatabase())

        then:
        statements.length == 1
        (statements[0] instanceof RawCompoundStatement) == compound

        where:
        sql                                    | compound
        "CALL myproc(1)"                       | true
        "-- run it\nCALL myproc(1)"            | true
        "/* run it */ CALL myproc(1)"          | true
        "-- run it\nselect 1 from sysibm.sysdummy1" | false
    }

    def "load with sql as value or as 'sql' child"() {
        when:
        def changeFromValue = new RawSQLChange()
        try {
            changeFromValue.load(new liquibase.parser.core.ParsedNode(null, "sql").setValue("select * from x"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e1) {
            e1.printStackTrace()
        } catch (SetupException e1) {
            e1.printStackTrace()
        }

        def changeFromChild = new RawSQLChange()
        try {
            changeFromChild.load(new liquibase.parser.core.ParsedNode(null, "sql").addChild(null, "sql", "select * from y"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        changeFromValue.getSql() == "select * from x"
        changeFromChild.getSql() == "select * from y"

    }

}
