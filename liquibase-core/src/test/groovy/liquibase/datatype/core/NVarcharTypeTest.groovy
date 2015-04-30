package liquibase.datatype.core

import liquibase.database.core.*
import spock.lang.Specification
import spock.lang.Unroll

class NVarcharTypeTest extends Specification {

    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new NVarcharType()
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
        [13]         | new OracleDatabase()   | "NVARCHAR2(13)"
        []           | new MSSQLDatabase()    | "[nvarchar](1)"
        [13]         | new MSSQLDatabase()    | "[nvarchar](13)"
        [2147483647] | new MSSQLDatabase()    | "[nvarchar](MAX)"
        [13]         | new MySQLDatabase()    | "NVARCHAR(13)"
    }

}
