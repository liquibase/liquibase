package liquibase.datatype

import liquibase.database.core.*
import liquibase.datatype.core.*
import liquibase.sdk.database.MockDatabase

import spock.lang.Specification
import spock.lang.Unroll

public class DataTypeFactoryTest extends Specification {

    @Unroll("#featureName: #liquibaseString for #database")
    public void fromDescription() throws Exception {
        when:
        def liquibaseType = DataTypeFactory.getInstance().fromDescription(liquibaseString, database)
        def databaseType = liquibaseType.toDatabaseDataType(database)
        def autoIncrement = liquibaseType.metaClass.respondsTo(liquibaseType, "isAutoIncrement") && liquibaseType.isAutoIncrement()

        then:
        databaseString == databaseType.toString()
        expectedType == liquibaseType.getClass()
        expectedAutoIncrement == autoIncrement

        where:
        liquibaseString                                      | database              | databaseString                                       | expectedType  | expectedAutoIncrement
        "int"                                                | new MockDatabase()    | "INT"                                                | IntType       | false
        "varchar(255)"                                       | new MockDatabase()    | "VARCHAR(255)"                                       | VarcharType   | false
        "int{autoIncrement:true}"                            | new MockDatabase()    | "INT"                                                | IntType       | true
        "int{autoIncrement:false}"                           | new MockDatabase()    | "INT"                                                | IntType       | false
        "int{}"                                              | new MockDatabase()    | "INT"                                                | IntType       | false
        "varchar COLLATE Latin1_General_BIN"                 | new MockDatabase()    | "VARCHAR COLLATE Latin1_General_BIN"                 | VarcharType   | false
        "varchar(255) COLLATE Latin1_General_BIN"            | new MockDatabase()    | "VARCHAR(255) COLLATE Latin1_General_BIN"            | VarcharType   | false
        "character varying(256)"                             | new MockDatabase()    | "VARCHAR(256)"                                       | VarcharType   | false
        "serial8"                                            | new MockDatabase()    | "BIGINT"                                             | BigIntType    | true
        "int4"                                               | new MockDatabase()    | "INT"                                                | IntType       | false
        "serial4"                                            | new MockDatabase()    | "INT"                                                | IntType       | true
    }
}
