package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification
import spock.lang.Unroll

class BitTypeTest extends Specification {

    @Unroll
    def "toDatabaseType '#input' on #database.shortName"() {
        when:
        def type = DataTypeFactory.getInstance().fromDescription(input, database)

        then:
        type instanceof BitType
        type.toDatabaseDataType(database).toString() == expected

        where:
        input      | database                   | expected
        "bit"      | new PostgresDatabase()     | "BIT"
        "bit(1)"   | new PostgresDatabase()     | "BIT(1)"
        "bit(8)"   | new PostgresDatabase()     | "BIT(8)"
        "bit"      | new MSSQLDatabase()        | "bit"
        "bit"      | new MySQLDatabase()        | "TINYINT"
        "bit(1)"   | new MySQLDatabase()        | "BIT(1)"
        "bit(5)"   | new MySQLDatabase()        | "BIT(5)"
        "bit"      | new MariaDBDatabase()      | "TINYINT(1)"
        "bit(8)"   | new MariaDBDatabase()      | "BIT(8)"
        "bit"      | new H2Database()           | "BOOLEAN"
        "bit(8)"   | new H2Database()           | "BIT(8)"
        "bit"      | new OracleDatabase()       | "NUMBER(1)"
        "bit"      | new DB2Database()          | "SMALLINT"
        "bit"      | new Db2zDatabase()         | "SMALLINT"
        "bit"      | new DerbyDatabase()        | "SMALLINT"
        "bit"      | new FirebirdDatabase()     | "BOOLEAN"
        "bit"      | new SybaseDatabase()       | "BIT"
        "bit"      | new SybaseASADatabase()    | "BIT"
    }

    @Unroll("#featureName: #value for #database.shortName")
    def "objectToSql - numeric values stay numeric"() {
        when:
        def type = new BitType()

        then:
        type.objectToSql(value, database) == expected

        where:
        value | database                | expected
        0     | new PostgresDatabase()  | "B'0'"
        1     | new PostgresDatabase()  | "B'1'"
        0     | new MSSQLDatabase()     | "0"
        1     | new MSSQLDatabase()     | "1"
        0     | new MySQLDatabase()     | "0"
        1     | new MySQLDatabase()     | "1"
        null  | new PostgresDatabase()  | null
        "0"   | new PostgresDatabase()  | "B'0'"
        "1"   | new PostgresDatabase()  | "B'1'"
        "0"   | new MSSQLDatabase()     | "0"
        "1"   | new MSSQLDatabase()     | "1"
    }

    @Unroll("#featureName: PostgreSQL bit string #value")
    def "objectToSql handles PostgreSQL bit strings"() {
        when:
        def type = new BitType()
        def database = new PostgresDatabase()

        then:
        type.objectToSql(value, database) == expected

        where:
        value              | expected
        "b'0'"             | "b'0'"
        "b'1'"             | "b'1'"
        "b'101010'"        | "b'101010'"
        "b'1'::bit"        | "b'1'::bit"
        "b'101'::bit(3)"   | "b'101'::bit(3)"
    }
}
