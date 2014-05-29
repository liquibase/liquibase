package liquibase.datatype.core

import liquibase.database.core.DerbyDatabase
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
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
        [13]         | new MSSQLDatabase()    | "CHAR(13)"
        [2147483647] | new MSSQLDatabase()    | "CHAR(2147483647)"
        [13]         | new MySQLDatabase()    | "CHAR(13)"
    }

}
