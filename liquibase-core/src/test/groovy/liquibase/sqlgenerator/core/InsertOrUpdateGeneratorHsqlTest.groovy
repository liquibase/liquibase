package liquibase.sqlgenerator.core

import liquibase.database.core.HsqlDatabase
import liquibase.statement.core.InsertOrUpdateStatement
import spock.lang.Specification

class InsertOrUpdateGeneratorHsqlTest extends Specification {

    def 'should get update statement'() {
        given:
        def stat = new InsertOrUpdateStatement(null, null, "testtable", "id_pk", true)
        stat.addColumnValue("column1", "value1")
        stat.addColumnValue("column2", "value2")
        def where = "id_pk = '1'"

        when:
        def result = new InsertOrUpdateGeneratorHsql().getUpdateStatement(stat, new HsqlDatabase(), where, null)

        then:
        result == "UPDATE testtable SET column1 = 'value1',column2 = 'value2' WHERE id_pk = '1'"
    }

    def 'should get update statement without where'() {
        given:
        def stat = new InsertOrUpdateStatement(null, null, "testtable", "id_pk", true)
        stat.addColumnValue("column1", "value1")

        when:
        def result = new InsertOrUpdateGeneratorHsql().getUpdateStatement(stat, new HsqlDatabase(), null, null)

        then:
        result == "UPDATE testtable SET column1 = 'value1'"
    }
}
