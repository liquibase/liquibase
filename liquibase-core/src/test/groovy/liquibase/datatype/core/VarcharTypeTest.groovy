package liquibase.datatype.core

import liquibase.database.core.DerbyDatabase
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
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
        [13]         | new MSSQLDatabase()    | false             | "VARCHAR(13)"
        [2147483647] | new MSSQLDatabase()    | false             | "VARCHAR(MAX)"
        [13]         | new MySQLDatabase()    | false             | "VARCHAR(13)"
    }
}
