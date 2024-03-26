package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification

class SmallIntTypeTest extends Specification{

            def "toDatabaseType '#input' on #database.shortName"() {

            when:
            def type = DataTypeFactory.getInstance().fromDescription(input, database)

            then:
            type instanceof SmallIntType
            type.toDatabaseDataType(database).toString() == expected

            where:
            input           | database               | expected
            "smallint"      | new H2Database()       | "SMALLINT"
            "smallint(20)"  | new H2Database()       | "SMALLINT"
            "smallint"      | new MSSQLDatabase()    | "smallint"
            "smallint"      | new PostgresDatabase() | "SMALLINT"
            "smallint"      | new OracleDatabase()   | "NUMBER(5)"
            "smallint"      | new DerbyDatabase()    | "SMALLINT"
            "smallint"      | new MySQLDatabase()    | "SMALLINT"
            "smallint"      | new FirebirdDatabase() | "SMALLINT"
            "smallint"      | new InformixDatabase() | "SMALLINT"

            //These are the some of the parsing combinations being tested for SmallIntType for some of the supported DBs.
            // Some of these scenarios are being also tested in DataTypeFactoryTest class.
        }
}
