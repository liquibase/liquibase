package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.database.core.MockDatabase
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

    @Unroll
    def "objectToSql escapes values to prevent SQL injection - #desc"() {
        when:
        def charType = new CharType()

        then:
        charType.objectToSql(input, database) == expected

        where:
        desc                                          | input                                | database               | expected
        "plain string"                                | "simple"                             | new H2Database()       | "'simple'"
        "single quote doubled - H2"                   | "O'Brien"                            | new H2Database()       | "'O''Brien'"
        "single quote doubled - PostgreSQL"           | "O'Brien"                            | new PostgresDatabase() | "'O''Brien'"
        "single quote doubled - Oracle"               | "O'Brien"                            | new OracleDatabase()   | "'O''Brien'"
        "single quote doubled - MSSQL"                | "O'Brien"                            | new MSSQLDatabase()    | "'O''Brien'"
        "single quote doubled - MySQL"                | "O'Brien"                            | new MySQLDatabase()    | "'O''Brien'"
        "injection: statement terminator - H2"        | "'; DROP TABLE DATABASECHANGELOG--"  | new H2Database()       | "'''; DROP TABLE DATABASECHANGELOG--'"
        "injection: statement terminator - MySQL"     | "'; DROP TABLE DATABASECHANGELOG--"  | new MySQLDatabase()    | "'''; DROP TABLE DATABASECHANGELOG--'"
        "injection: boolean bypass - H2"              | "' OR '1'='1"                        | new H2Database()       | "''' OR ''1''=''1'"
        "MySQL backslash is escaped"                  | "foo\\bar"                           | new MySQLDatabase()    | "'foo\\\\bar'"
        "null returns null"                           | null                                 | new H2Database()       | null
        "literal null string returns null"            | "null"                               | new H2Database()       | null
    }
}
