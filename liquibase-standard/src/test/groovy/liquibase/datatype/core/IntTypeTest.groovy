package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification

class IntTypeTest extends Specification{
    def "toDatabaseType '#input' on #database.shortName"() {

        when:
        def type = DataTypeFactory.getInstance().fromDescription(input, database)

        then:
        type instanceof IntType
        type.toDatabaseDataType(database).toString() == expected

        where:
        input                         | database               | expected
        "int"                         | new H2Database()       | "INT"
        "int(2)"                      | new H2Database()       | "TINYINT"
        "int(4)"                      | new H2Database()       | "SMALLINT"
        "int(7)"                      | new H2Database()       | "INTEGER"
        "int(20)"                     | new H2Database()       | "BIGINT"
        "int(20)"                     | new H2Database()       | "BIGINT"
        "int{autoIncrement:true}"     | new H2Database()       | "INT"
        "int(2){autoIncrement:true}"  | new H2Database()       | "TINYINT"
        "int(4){autoIncrement:true}"  | new H2Database()       | "SMALLINT"
        "int(7){autoIncrement:true}"  | new H2Database()       | "INTEGER"
        "int(20){autoIncrement:true}" | new H2Database()       | "BIGINT"
        "int(20){autoIncrement:true}" | new H2Database()       | "BIGINT"
        "int"                         | new MSSQLDatabase()    | "int"
        "int"                         | new PostgresDatabase() | "INTEGER"
        "int"                         | new OracleDatabase()   | "INTEGER"
        "int"                         | new DerbyDatabase()    | "INTEGER"
        "int"                         | new MySQLDatabase()    | "INT"
        "int"                         | new FirebirdDatabase() | "INT"
        "int"                         | new HsqlDatabase()     | "INT"
        "int"                         | new InformixDatabase() | "INT"
        "int"                         | new SybaseDatabase()   | "INT"
        "int"                         | new SQLiteDatabase()   | "INTEGER"

        //These are the some of the parsing combinations being tested for IntType for some of the supported DBs.
        // Some of these scenarios are being also tested in DataTypeFactoryTest class.
    }
}
