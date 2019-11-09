package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.database.core.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class DoubleTypeTest extends Specification {

    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new DoubleType()
        for (param in params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        params | database               | expected
        [22]   | new MySQLDatabase()    | "DOUBLE"
        [7, 3] | new MySQLDatabase()    | "DOUBLE(7, 3)"
        [22]   | new DB2Database()      | "DOUBLE"
        [22]   | new DerbyDatabase()    | "DOUBLE"
        [22]   | new HsqlDatabase()     | "DOUBLE"
        [22]   | new MSSQLDatabase()    | "float(53)"
        [22]   | new PostgresDatabase() | "DOUBLE PRECISION"
        [22]   | new InformixDatabase() | "DOUBLE PRECISION"
        []     | new OracleDatabase()   | "FLOAT(24)"
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
