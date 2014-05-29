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
        def type = new VarcharType()
        for (param in params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        params       | database               | expected
        [13]         | new DerbyDatabase()    | "VARCHAR(13)"
        [13]         | new HsqlDatabase()     | "VARCHAR(13)"
        [13]         | new PostgresDatabase() | "VARCHAR(13)"
        [13]         | new OracleDatabase()   | "VARCHAR2(13)"
        [13]         | new MSSQLDatabase()    | "VARCHAR(13)"
        [2147483647] | new MSSQLDatabase()    | "VARCHAR(MAX)"
        [13]         | new MySQLDatabase()    | "VARCHAR(13)"
    }
}
