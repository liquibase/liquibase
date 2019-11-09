package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.database.core.MockDatabase
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException

public class UpdateDataChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new UpdateDataChange()
        change.setTableName("TABLE_NAME");

        then:
        change.getConfirmationMessage() == "Data updated in TABLE_NAME"
    }


    @Override
    protected String getExpectedChangeName() {
        return "update"
    }

    def "checkStatus"() {
        when:
        def database = new MockDatabase()
        def change = new UpdateDataChange()

        then:
        assert change.checkStatus(database).status == ChangeStatus.Status.unknown
        assert change.checkStatus(database).message == "Cannot check updateData status"
    }

    def "load with whereParams"() {
        when:
        def change = new UpdateDataChange()
        def whereParams = new ParsedNode(null, "whereParams")
                .addChild(new ParsedNode(null, "param").addChild(null, "valueNumeric", "134"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "other_val", value: "asdf"]))
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "updateData").addChild(null, "tableName", "updateTest").addChild(whereParams), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.tableName == "updateTest"
        change.whereParams.size() == 2
        change.whereParams[0].valueNumeric == 134
        change.whereParams[1].name == "other_val"
        change.whereParams[1].value == "asdf"
    }

}
