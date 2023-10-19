package liquibase.sqlgenerator.core

import liquibase.database.core.HsqlDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.statement.core.CreateViewStatement
import liquibase.Scope
import spock.lang.Specification

class CreateViewGeneratorTest extends Specification {

    def "creates a view from a sql"() {
        when:
        def selectQuery = "SELECT SYSDATE FROM DUAL"
        def statement = new CreateViewStatement("PUBLIC", "schema", "my_view", selectQuery, false)
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, new OracleDatabase())

        then:
        generators.size() > 0
        generators[0] instanceof CreateViewGenerator

        when:
        def sql = SqlGeneratorFactory.instance.generateSql(statement, new OracleDatabase())

        then:
        sql.length == 1
        sql[0].toString() == "CREATE VIEW PUBLIC.my_view AS " + selectQuery + ";"
    }

    def "creates a replace view from a sql"() {
        when:
        def selectQuery = "SELECT SYSDATE FROM DUAL"
        def statement = new CreateViewStatement("PUBLIC", "schema", "my_view", selectQuery, true)
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, new OracleDatabase())

        then:
        generators.size() > 0
        generators[0] instanceof CreateViewGenerator

        when:
        def sql = SqlGeneratorFactory.instance.generateSql(statement, new OracleDatabase())

        then:
        sql.length == 1
        sql[0].toString() == "CREATE OR REPLACE VIEW PUBLIC.my_view AS " + selectQuery + ";"
    }

    def "replace is added even when a select replace is used"() {
        when:
        def selectQuery = "SELECT REPLACE('The quick brown dog', 'dog', 'fox') FROM DUAL"
        def statement = new CreateViewStatement("PUBLIC", "schem", "my_view", selectQuery, true)
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, new OracleDatabase())

        then:
        generators.size() > 0
        generators[0] instanceof CreateViewGenerator

        when:
        def sql = SqlGeneratorFactory.instance.generateSql(statement, new OracleDatabase())

        then:
        sql.length == 1
        sql[0].toString() == "CREATE OR REPLACE VIEW PUBLIC.my_view AS " + selectQuery + ";"
    }

    def "creates drop view statement for Postgres when replace is true and alwaysDropInsteadOfReplace property is set"() {
        given:
        System.setProperty("liquibase.alwaysDropInsteadOfReplace", alwaysDropInsteadOfReplacePropValue)

        when:
        def selectQuery = "SELECT SYSDATE FROM DUAL"
        def statement = new CreateViewStatement("PUBLIC", "schema", "my_view", selectQuery, true)
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, database)

        then:
        generators.size() > 0
        generators[0] instanceof CreateViewGenerator

        when:
        def sql = SqlGeneratorFactory.instance.generateSql(statement, database)

        then:
        sql.length == expectedNumOfSqlStatements
        if (sql.length == 1) {
            sql[0].toString() == "CREATE OR REPLACE VIEW PUBLIC.my_view AS " + selectQuery + ";"
        }
        else {
            sql[0].toString() == "DROP VIEW IF EXISTS PUBLIC.my_view;"
            sql[1].toString() == "CREATE OR REPLACE VIEW PUBLIC.my_view AS " + selectQuery + ";"
        }

        where:
        database | alwaysDropInsteadOfReplacePropValue | expectedNumOfSqlStatements
        new HsqlDatabase() | "false" | 2
        new HsqlDatabase() | "true" | 2
        new OracleDatabase() | "false" | 1
        new OracleDatabase() | "true" | 1
        new PostgresDatabase() | "false" | 1
        new PostgresDatabase() | "true" | 2
    }

    def "full definition is used if it contains CREATE OR ALTER"() {
        when:
        def fullDefinition =
"CREATE OR ALTER VIEW dbo.myView AS\nSELECT compvalcol, idxclustercol\nFROM primary_table"

        def statement = new CreateViewStatement("PUBLIC", "schem", "my_view", fullDefinition, true)
        statement.setFullDefinition(true)
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, new MSSQLDatabase())

        then:
        generators.size() > 0
        generators[0] instanceof CreateViewGenerator

        when:
        def sql = SqlGeneratorFactory.instance.generateSql(statement, new MSSQLDatabase())

        then:
        sql.length == 1
        sql[0].toString() == fullDefinition + ";"
    }

}
