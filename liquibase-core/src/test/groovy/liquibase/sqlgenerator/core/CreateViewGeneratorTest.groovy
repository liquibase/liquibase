package liquibase.sqlgenerator.core


import liquibase.database.core.OracleDatabase
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.statement.core.CreateViewStatement
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


}
