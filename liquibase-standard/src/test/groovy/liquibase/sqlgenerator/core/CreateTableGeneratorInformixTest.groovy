package liquibase.sqlgenerator.core

import liquibase.database.core.InformixDatabase
import liquibase.datatype.core.IntType
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.statement.AutoIncrementConstraint
import liquibase.statement.ColumnConstraint
import liquibase.statement.PrimaryKeyConstraint
import liquibase.statement.core.CreateTableStatement
import spock.lang.Specification

class CreateTableGeneratorInformixTest extends Specification {

    def "correct generator is used"() {
        when:
        def statement = new CreateTableStatement("cat", "schem", "tab")
        def generators = SqlGeneratorFactory.instance.getGenerators(statement, new InformixDatabase())

        then:
        generators.size() > 0
        generators[0] instanceof CreateTableGeneratorInformix

        when:
        statement.addColumn("id", new IntType(), [new PrimaryKeyConstraint().addColumns("id"), new AutoIncrementConstraint("id")] as ColumnConstraint[])
        def sql = SqlGeneratorFactory.instance.generateSql(statement, new InformixDatabase())

        then:
        sql.length == 1
        sql[0].toString() == "CREATE TABLE cat:schem.tab (id INT, PRIMARY KEY (id));"
    }
}
