package liquibase.sqlgenerator.core

import liquibase.database.core.SnowflakeDatabase
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.core.DropProcedureStatement
import spock.lang.Specification
import spock.lang.Unroll

class DropProcedureGeneratorSnowflakeTest extends Specification {

    @Unroll
    def " Should produce correct sql for drop Procedure"() {
        given:

        DropProcedureStatement statement = new DropProcedureStatement(catalogName, schemaName, procedureName)
        Sql[] sql = new DropProcedureGeneratorSnowflake().generateSql(statement, new SnowflakeDatabase(), new SqlGeneratorChain())

        expect:
        sql != null
        sql.first().toSql() == expectedSql

        where:
        catalogName | schemaName | procedureName            | expectedSql
        null        | null       | "myProcedure(INT, DATE)" | "DROP PROCEDURE myProcedure(INT, DATE)"
        "myCatalog" | "mySchema" | "myProcedure(INT)"       | "DROP PROCEDURE mySchema.myProcedure(INT)"
        "myCatalog" | "mySchema" | "myProcedure()"          | "DROP PROCEDURE mySchema.myProcedure()"
        "myCatalog" | "mySchema" | "myProcedure"            | "DROP PROCEDURE mySchema.myProcedure()"
    }

}
