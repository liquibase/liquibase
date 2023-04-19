package liquibase.datatype.core

import liquibase.database.core.DerbyDatabase
import liquibase.database.core.FirebirdDatabase
import liquibase.database.core.H2Database
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.InformixDatabase
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.database.core.SQLiteDatabase
import liquibase.database.core.SybaseDatabase
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification
import spock.lang.Unroll

class IntTypeTest extends Specification{
    @Unroll
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
