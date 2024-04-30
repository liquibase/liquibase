package liquibase.sqlgenerator.core

import liquibase.change.ColumnConfig
import liquibase.database.core.*
import liquibase.sqlgenerator.MockSqlGeneratorChain
import liquibase.sqlgenerator.SqlGenerator
import liquibase.statement.core.AddPrimaryKeyStatement
import spock.lang.Specification
import spock.lang.Unroll

class AddPrimaryKeyGeneratorAllVariantsTest extends Specification {

    protected static final String TABLE_NAME = "AddPkTest"
    protected static final String COLUMN_NAME = "colToMakePk"
    protected static final String COLUMN_NAME2 = "coltoMakePk2"
    protected static final String CONSTRAINT_NAME = "PK_TEST"
    private static final String INDEX_NAME = "pkIndex"

    @Unroll
    def "test validation of forIndexName for #database"() {
        when:
            SqlGenerator<AddPrimaryKeyStatement> generatorUnderTest = new AddPrimaryKeyGenerator()
            AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(null, null, TABLE_NAME, [new ColumnConfig().setName(COLUMN_NAME), new ColumnConfig().setName(COLUMN_NAME2)] as ColumnConfig[], CONSTRAINT_NAME)
            statement.setForIndexName(INDEX_NAME)

        then:
            validationErrors == generatorUnderTest.validate(statement, database, new MockSqlGeneratorChain()).errorMessages

        where:
            database                   | validationErrors
            new H2Database()           | ["forIndexName is not allowed on h2"]
            new SybaseDatabase()       | ["forIndexName is not allowed on sybase"]
            new FirebirdDatabase()     | ["forIndexName is not allowed on firebird"]
            new H2Database()           | ["forIndexName is not allowed on h2"]
            new UnsupportedDatabase()  | ["forIndexName is not allowed on unsupported"]
            new MSSQLDatabase()        | ["forIndexName is not allowed on mssql"]
            new OracleDatabase()       | []
            new SybaseASADatabase()    | ["forIndexName is not allowed on asany"]
            new Db2zDatabase()         | []
            new Ingres9Database()      | ["forIndexName is not allowed on ingres"]
            new DerbyDatabase()        | ["forIndexName is not allowed on derby"]
            new CockroachDatabase()    | []
            new EnterpriseDBDatabase() | []
            new MariaDBDatabase()      | ["forIndexName is not allowed on mariadb"]
            new HsqlDatabase()         | ["forIndexName is not allowed on hsqldb"]
            new DB2Database()          | []
            new MySQLDatabase()        | ["forIndexName is not allowed on mysql"]
            new PostgresDatabase()     | []
            new InformixDatabase()     | ["forIndexName is not allowed on informix"]
    }

}
