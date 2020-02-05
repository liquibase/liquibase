package liquibase.change.core

import liquibase.change.ChangeStatus
import liquibase.change.StandardChangeTest
import liquibase.database.core.MySQLDatabase
import liquibase.sdk.database.MockDatabase
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sql.Sql
import liquibase.sqlgenerator.core.UpdateGenerator

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
        def where = "colA = :value AND :name = :value"
        def change = new UpdateDataChange()
        def whereParams = new ParsedNode(null, "whereParams")
                .setValue([[param:[valueNumeric:134]]
                             ,[param:[name: "other_val", value: "asdf"]]
                ])

        change.load(new liquibase.parser.core.ParsedNode(null, "updateData")
                .addChild(null, "tableName", "updateTest")
                .setValue([ whereParams
                           ,[where: where]
                           ,[column: [name: "colB", value: "colBVal", type: "STRING"]]
                           ,[column: [name: "colC", valueNumeric: 5, type: "NUMERIC"]]
                ])
               , resourceSupplier.simpleResourceAccessor)

        def db = new MySQLDatabase()
        def stmts = change.generateStatements(db)

        Sql[] sqls = (new UpdateGenerator()).generateSql(stmts[0], db, null)

        then:
        def sql = sqls[0].toString()
        change.tableName == "updateTest"
        change.whereParams.size() == 2
        change.whereParams[0].valueNumeric == 134
        change.whereParams[1].name == "other_val"
        change.whereParams[1].value == "asdf"
        change.columns.size() == 2
        change.columns[0].name == "colB"
        change.columns[0].value == "colBVal"
        change.columns[1].name == "colC"
        change.columns[1].valueNumeric == 5
        change.where == where
    }

}
