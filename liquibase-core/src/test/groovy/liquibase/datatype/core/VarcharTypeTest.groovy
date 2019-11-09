package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.database.core.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class VarcharTypeTest extends Specification {
    @Unroll
    def "toDatabaseType"() {
        when:
        if (database instanceof HsqlDatabase && usingOracleSyntax) {
            database = Mock(HsqlDatabase) {
                isUsingOracleSyntax() >> true
            }
        }
        def type = new VarcharType()
        for (param in params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        params       | database               | usingOracleSyntax | expected
        [13]         | new DerbyDatabase()    | false             | "VARCHAR(13)"
        [13]         | new HsqlDatabase()     | false             | "VARCHAR(13)"
        [13]         | new HsqlDatabase()     | true              | "VARCHAR2(13)"
        [13]         | new PostgresDatabase() | false             | "VARCHAR(13)"
        [13]         | new OracleDatabase()   | false             | "VARCHAR2(13)"
        []           | new MSSQLDatabase()    | false             | "varchar(1)"
        [13]         | new MSSQLDatabase()    | false             | "varchar(13)"
        [2147483647] | new MSSQLDatabase()    | false             | "varchar(MAX)"
        [13]         | new MySQLDatabase()    | false             | "VARCHAR(13)"
    }

    def "too many parameters"() {
        when:
        def type = new VarcharType()
        type.addParameter(47)
        type.addParameter(11)
        type.validate(new MockDatabase())

        then:
        thrown UnexpectedLiquibaseException

    }
}
