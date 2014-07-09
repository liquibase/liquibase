package liquibase.datatype.core

import liquibase.database.core.DB2Database
import liquibase.database.core.DerbyDatabase
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.InformixDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
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
        [22]   | new MySQLDatabase()    | "DOUBLE(22)"
        [7,3]   | new MySQLDatabase()    | "DOUBLE(7, 3)"
        [22]   | new DB2Database()      | "DOUBLE"
        [22]   | new DerbyDatabase()    | "DOUBLE"
        [22]   | new HsqlDatabase()     | "DOUBLE"
        [22]   | new MSSQLDatabase()    | "FLOAT"
        [22]   | new PostgresDatabase() | "DOUBLE PRECISION"
        [22]   | new InformixDatabase() | "DOUBLE PRECISION"
        []     | new OracleDatabase()   | "FLOAT(24)"
    }
}
