package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.sdk.database.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class CharTypeTest extends Specification {
    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new CharType()
        for (param in params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        params       | database               | expected
        [13]         | new DerbyDatabase()    | "CHAR(13)"
        [13]         | new HsqlDatabase()     | "CHAR(13)"
        [13]         | new PostgresDatabase() | "CHAR(13)"
        [13]         | new OracleDatabase()   | "CHAR(13)"
        []           | new MSSQLDatabase()    | "char(1)"
        [13]         | new MSSQLDatabase()    | "char(13)"
        [2147483647] | new MSSQLDatabase()    | "char(8000)"
        [13]         | new MySQLDatabase()    | "CHAR(13)"
    }

    def "too many parameters"() {
        when:
        def type = new CharType()
        type.addParameter(47)
        type.addParameter(11)
        type.validate(new MockDatabase())

        then:
        thrown UnexpectedLiquibaseException

    }
}
