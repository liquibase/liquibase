package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification

class TinyIntTypeTest extends Specification{
    def "toDatabaseType '#input' on #database.shortName"() {

        when:
        def type = DataTypeFactory.getInstance().fromDescription(input, database)

        then:
        type instanceof TinyIntType
        type.toDatabaseDataType(database).toString() == expected

        where:
        input           | database               | expected
        "tinyint"      | new H2Database()       | "TINYINT"
        "tinyint(20)"  | new H2Database()       | "TINYINT"
        "tinyint"      | new MSSQLDatabase()    | "tinyint"
        "tinyint"      | new PostgresDatabase() | "SMALLINT"
        "tinyint"      | new OracleDatabase()   | "NUMBER(3)"
        "tinyint"      | new DerbyDatabase()    | "SMALLINT"
        "tinyint"      | new MySQLDatabase()    | "TINYINT"
        "tinyint"      | new FirebirdDatabase() | "SMALLINT"
        "tinyint"      | new InformixDatabase() | "TINYINT"

        //These are the some of the parsing combinations being tested for TinyIntType for some of the supported DBs.
        // Some of these scenarios are being also tested in DataTypeFactoryTest class.
    }
}
