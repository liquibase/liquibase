package liquibase.change.core

import liquibase.database.core.MSSQLDatabase
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.sql.Sql
import liquibase.sqlgenerator.core.DeleteGenerator
import spock.lang.Shared
import spock.lang.Specification

class DeleteDataChangeTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load with whereParams"() {
        when:
        def change = new DeleteDataChange()
        def whereParams = new ParsedNode(null, "whereParams")
                .addChild(new ParsedNode(null, "param").addChild(null, "valueNumeric", "134"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "other_val", value: "asdf"]))
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "delete").addChild(null, "tableName", "deleteTest").addChild(whereParams), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        def statements = change.generateStatements(new MockDatabase())
        DeleteGenerator generator = new DeleteGenerator();
        Sql[] sqls = generator.generateSql(statements[0], new MSSQLDatabase(),null)

        then:
        change.tableName == "deleteTest"
        change.whereParams.size() == 2
        change.whereParams[0].valueNumeric == 134
        change.whereParams[1].name == "other_val"
        change.whereParams[1].value == "asdf"
    }

}
