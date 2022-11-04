package liquibase.sqlgenerator.core

import liquibase.database.core.HsqlDatabase
import liquibase.statement.core.InsertOrUpdateStatement
import spock.lang.Specification

class InsertOrUpdateGeneratorHsqlTest extends Specification {

    def 'should get update statement for update only'() {
        given:
        def stat = new InsertOrUpdateStatement(null, null, "testtable", "id_pk", true)
        stat.addColumnValue("column1", "value1")
        stat.addColumnValue("column2", "value2")
        def where = "id_pk = '1'"

        when:
        def result = new InsertOrUpdateGeneratorHsql().getUpdateStatement(stat, new HsqlDatabase(), where, null)

        then:
        result == "UPDATE testtable SET column1 = 'value1', column2 = 'value2' WHERE id_pk = '1';\n"
    }

    def 'should get update statement for insert or update'() {
        given:
        def stat = new InsertOrUpdateStatement(null, null, "testtable", "id_pk", false)
        stat.addColumnValue("column1", "value1")

        when:
        def result = new InsertOrUpdateGeneratorHsql().getUpdateStatement(stat, new HsqlDatabase(), null, null)

        then:
        result == "UPDATE SET column1 = 'value1'"
    }

    def 'should prepare merge statement for record check'() {
        given:
        def stat = new InsertOrUpdateStatement(null, null, "testtable", "id_pk", false)

        when:
        def result = new InsertOrUpdateGeneratorHsql().getRecordCheck(stat, new HsqlDatabase(), "id='1'")

        then:
        result == "MERGE INTO testtable USING (VALUES (1)) ON id='1' WHEN NOT MATCHED THEN "
    }
}
