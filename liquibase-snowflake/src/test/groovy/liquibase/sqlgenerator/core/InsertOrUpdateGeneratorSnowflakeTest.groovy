package liquibase.sqlgenerator.core

import liquibase.database.core.SnowflakeDatabase
import liquibase.sql.Sql
import liquibase.statement.SequenceNextValueFunction
import liquibase.statement.core.UpdateStatement
import spock.lang.Specification

class InsertOrUpdateGeneratorSnowflakeTest extends Specification {

    def "testUpdateGeneratorWithSequenceMustSucceed"() {
        when:
        SnowflakeDatabase database = new SnowflakeDatabase()

        UpdateGenerator generator = new UpdateGenerator()
        UpdateStatement statement = new UpdateStatement("mycatalog", "myschema", "mytable")

        SequenceNextValueFunction sequenceNext = new SequenceNextValueFunction("myschema", "mysequence")
        statement.addNewColumnValue("mycolumn", sequenceNext)

        Sql[] sql = generator.generateSql(statement, database, null)

        then:
        sql != null
        sql.size() == 1
        sql[0].toSql() == 'UPDATE mycatalog.myschema.mytable SET mycolumn = myschema.mysequence.nextval'
    }
}
