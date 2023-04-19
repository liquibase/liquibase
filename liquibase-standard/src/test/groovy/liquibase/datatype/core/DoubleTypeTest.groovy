package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.datatype.DataTypeFactory
import liquibase.exception.UnexpectedLiquibaseException
import spock.lang.Specification
import spock.lang.Unroll

class DoubleTypeTest extends Specification {

    @Unroll
    def "toDatabaseType '#input' on #database.shortName"() {
        when:
        def type = DataTypeFactory.getInstance().fromDescription(input, database)

        then:
        type instanceof DoubleType
        type.toDatabaseDataType(database).toString() == expected

        where:
        input                            | database               | expected
        "double(22)"                     | new MySQLDatabase()    | "DOUBLE"
        "double(7,3)"                    | new MySQLDatabase()    | "DOUBLE(7, 3)"
        "double precision(7,3)"          | new MySQLDatabase()    | "DOUBLE PRECISION(7, 3)"
        "double(7,3) unsigned"           | new MySQLDatabase()    | "DOUBLE(7, 3) UNSIGNED"
        "double precision(7,3) unsigned" | new MySQLDatabase()    | "DOUBLE PRECISION(7, 3) UNSIGNED"
        "double"                         | new DB2Database()      | "DOUBLE"
        "double"                         | new DerbyDatabase()    | "DOUBLE"
        "double"                         | new HsqlDatabase()     | "DOUBLE"
        "double"                         | new H2Database()       | "DOUBLE PRECISION"
        "double precision"               | new H2Database()       | "DOUBLE PRECISION"
        "double(20)"                     | new H2Database()       | "DOUBLE PRECISION"
        "double"                         | new MSSQLDatabase()    | "float(53)"
        "double"                         | new PostgresDatabase() | "DOUBLE PRECISION"
        "double"                         | new InformixDatabase() | "DOUBLE PRECISION"
        "double"                         | new FirebirdDatabase() | "DOUBLE PRECISION"
        "double"                         | new OracleDatabase()   | "DOUBLE PRECISION"
    }

    def "too many parameters"() {
        when:
        def type = new DoubleType()
        type.addParameter(47)
        type.addParameter(11)
        type.addParameter(42)
        type.validate(new MockDatabase())

        then:
        thrown UnexpectedLiquibaseException

    }
}
