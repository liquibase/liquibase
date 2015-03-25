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

    @Unroll("#featureName: #object for #database")
    public void fromObject() throws Exception {
        when:
        def liquibaseType = DataTypeFactory.getInstance().fromObject(object, database)

        then:
        liquibaseType.objectToSql(object, database) == expectedSql
        liquibaseType.getClass() == expectedType

        where:
        object                       | database           | expectedType | expectedSql
        Integer.valueOf("10000000")  | new MockDatabase() | IntType      | "10000000"
        Long.valueOf("10000000")     | new MockDatabase() | BigIntType   | "10000000"
        new BigInteger("10000000")   | new MockDatabase() | BigIntType   | "10000000"
        Float.valueOf("10000000.0")  | new MockDatabase() | FloatType    | "1.0E7"
        Float.valueOf("10000000.1")  | new MockDatabase() | FloatType    | "1.0E7"
        Double.valueOf("10000000.0") | new MockDatabase() | DoubleType   | "1.0E7"
        Double.valueOf("10000000.1") | new MockDatabase() | DoubleType   | "1.00000001E7"
        new BigDecimal("10000000.0") | new MockDatabase() | DecimalType  | "10000000"
        new BigDecimal("10000000.1") | new MockDatabase() | DecimalType  | "10000000.1"
        "10000000"                   | new MockDatabase() | VarcharType  | "'10000000'"
    }
}
