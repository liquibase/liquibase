package liquibase.datatype

import liquibase.database.DatabaseFactory
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.core.BigIntType
import liquibase.datatype.core.BlobType
import liquibase.datatype.core.BooleanType
import liquibase.datatype.core.CharType
import liquibase.datatype.core.ClobType
import liquibase.datatype.core.CurrencyType
import liquibase.datatype.core.DateTimeType
import liquibase.datatype.core.DateType
import liquibase.datatype.core.DecimalType
import liquibase.datatype.core.DoubleType
import liquibase.datatype.core.FloatType;
import liquibase.datatype.core.IntType
import liquibase.datatype.core.MediumIntType
import liquibase.datatype.core.NCharType
import liquibase.datatype.core.NVarcharType
import liquibase.datatype.core.NumberType
import liquibase.datatype.core.SmallIntType
import liquibase.datatype.core.TimeType
import liquibase.datatype.core.TimestampType
import liquibase.datatype.core.TinyIntType
import liquibase.datatype.core.UUIDType
import liquibase.datatype.core.UnknownType;
import liquibase.datatype.core.VarcharType
import liquibase.sdk.database.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

public class DataTypeFactoryTest extends Specification {

    @Unroll("#featureName: #liquibaseString for #database")
    public void fromDescription() throws Exception {
        when:
        def liquibaseType = DataTypeFactory.getInstance().fromDescription(liquibaseString, database)
        def databaseType = liquibaseType.toDatabaseDataType(database)
        def autoIncrement = (liquibaseType instanceof IntType && ((IntType) liquibaseType).isAutoIncrement()) \
                || (liquibaseType instanceof BigIntType && ((BigIntType) liquibaseType).isAutoIncrement())

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
        "bigint"                                             | new MSSQLDatabase()   | "[bigint]"                                           | BigIntType    | false
        "[bigint]"                                           | new MSSQLDatabase()   | "[bigint]"                                           | BigIntType    | false
        "binary"                                             | new MSSQLDatabase()   | "[binary](1)"                                        | BlobType      | false
        "[binary]"                                           | new MSSQLDatabase()   | "[binary](1)"                                        | BlobType      | false
        "binary(8000)"                                       | new MSSQLDatabase()   | "[binary](8000)"                                     | BlobType      | false
        "[binary](8000)"                                     | new MSSQLDatabase()   | "[binary](8000)"                                     | BlobType      | false
        "bit"                                                | new MSSQLDatabase()   | "[bit]"                                              | BooleanType   | false
        "[bit]"                                              | new MSSQLDatabase()   | "[bit]"                                              | BooleanType   | false
        "blob"                                               | new MSSQLDatabase()   | "[varbinary](MAX)"                                   | BlobType      | false
        "boolean"                                            | new MSSQLDatabase()   | "[bit]"                                              | BooleanType   | false
        "char"                                               | new MSSQLDatabase()   | "[char](1)"                                          | CharType      | false
        "[char]"                                             | new MSSQLDatabase()   | "[char](1)"                                          | CharType      | false
        "char(8000)"                                         | new MSSQLDatabase()   | "[char](8000)"                                       | CharType      | false
        "[char](8000)"                                       | new MSSQLDatabase()   | "[char](8000)"                                       | CharType      | false
        "clob"                                               | new MSSQLDatabase()   | "[nvarchar](MAX)"                                    | ClobType      | false
        "currency"                                           | new MSSQLDatabase()   | "[money]"                                            | CurrencyType  | false
        "date"                                               | new MSSQLDatabase()   | "[date]"                                             | DateType      | false
        "[date]"                                             | new MSSQLDatabase()   | "[date]"                                             | DateType      | false
        "datetime"                                           | new MSSQLDatabase()   | "[datetime]"                                         | DateTimeType  | false
        "[datetime]"                                         | new MSSQLDatabase()   | "[datetime]"                                         | DateTimeType  | false
        "datetime2"                                          | new MSSQLDatabase()   | "[datetime2](7)"                                     | DateTimeType  | false
        "[datetime2]"                                        | new MSSQLDatabase()   | "[datetime2](7)"                                     | DateTimeType  | false
        "datetime2(6)"                                       | new MSSQLDatabase()   | "[datetime2](6)"                                     | DateTimeType  | false
        "[datetime2](6)"                                     | new MSSQLDatabase()   | "[datetime2](6)"                                     | DateTimeType  | false
        "decimal"                                            | new MSSQLDatabase()   | "[decimal](18, 0)"                                   | DecimalType   | false
        "[decimal]"                                          | new MSSQLDatabase()   | "[decimal](18, 0)"                                   | DecimalType   | false
        "decimal(19)"                                        | new MSSQLDatabase()   | "[decimal](19, 0)"                                   | DecimalType   | false
        "[decimal](19)"                                      | new MSSQLDatabase()   | "[decimal](19, 0)"                                   | DecimalType   | false
        "decimal(19, 2)"                                     | new MSSQLDatabase()   | "[decimal](19, 2)"                                   | DecimalType   | false
        "[decimal](19, 2)"                                   | new MSSQLDatabase()   | "[decimal](19, 2)"                                   | DecimalType   | false
        "double"                                             | new MSSQLDatabase()   | "[float](53)"                                        | DoubleType    | false
        "float"                                              | new MSSQLDatabase()   | "[float](53)"                                        | FloatType     | false
        "[float]"                                            | new MSSQLDatabase()   | "[float](53)"                                        | FloatType     | false
        "float(53)"                                          | new MSSQLDatabase()   | "[float](53)"                                        | FloatType     | false
        "[float](53)"                                        | new MSSQLDatabase()   | "[float](53)"                                        | FloatType     | false
        "image"                                              | new MSSQLDatabase()   | "[image]"                                            | BlobType      | false
        "[image]"                                            | new MSSQLDatabase()   | "[image]"                                            | BlobType      | false
        "int"                                                | new MSSQLDatabase()   | "[int]"                                              | IntType       | false
        "[int]"                                              | new MSSQLDatabase()   | "[int]"                                              | IntType       | false
        "integer"                                            | new MSSQLDatabase()   | "[int]"                                              | IntType       | false
        "mediumint"                                          | new MSSQLDatabase()   | "[int]"                                              | MediumIntType | false
        "money"                                              | new MSSQLDatabase()   | "[money]"                                            | CurrencyType  | false
        "[money]"                                            | new MSSQLDatabase()   | "[money]"                                            | CurrencyType  | false
        "nchar"                                              | new MSSQLDatabase()   | "[nchar](1)"                                         | NCharType     | false
        "[nchar]"                                            | new MSSQLDatabase()   | "[nchar](1)"                                         | NCharType     | false
        "nchar(4000)"                                        | new MSSQLDatabase()   | "[nchar](4000)"                                      | NCharType     | false
        "[nchar](4000)"                                      | new MSSQLDatabase()   | "[nchar](4000)"                                      | NCharType     | false
        "ntext"                                              | new MSSQLDatabase()   | "[ntext]"                                            | ClobType      | false
        "[ntext]"                                            | new MSSQLDatabase()   | "[ntext]"                                            | ClobType      | false
        "number"                                             | new MSSQLDatabase()   | "[numeric](18, 0)"                                   | NumberType    | false
        "numeric"                                            | new MSSQLDatabase()   | "[numeric](18, 0)"                                   | NumberType    | false
        "[numeric]"                                          | new MSSQLDatabase()   | "[numeric](18, 0)"                                   | NumberType    | false
        "numeric(19)"                                        | new MSSQLDatabase()   | "[numeric](19, 0)"                                   | NumberType    | false
        "[numeric](19)"                                      | new MSSQLDatabase()   | "[numeric](19, 0)"                                   | NumberType    | false
        "numeric(19, 2)"                                     | new MSSQLDatabase()   | "[numeric](19, 2)"                                   | NumberType    | false
        "[numeric](19, 2)"                                   | new MSSQLDatabase()   | "[numeric](19, 2)"                                   | NumberType    | false
        "nvarchar"                                           | new MSSQLDatabase()   | "[nvarchar](1)"                                      | NVarcharType  | false
        "[nvarchar]"                                         | new MSSQLDatabase()   | "[nvarchar](1)"                                      | NVarcharType  | false
        "nvarchar(4000)"                                     | new MSSQLDatabase()   | "[nvarchar](4000)"                                   | NVarcharType  | false
        "[nvarchar](4000)"                                   | new MSSQLDatabase()   | "[nvarchar](4000)"                                   | NVarcharType  | false
        "nvarchar(MAX)"                                      | new MSSQLDatabase()   | "[nvarchar](MAX)"                                    | NVarcharType  | false
        "[nvarchar](MAX)"                                    | new MSSQLDatabase()   | "[nvarchar](MAX)"                                    | NVarcharType  | false
        "real"                                               | new MSSQLDatabase()   | "[real]"                                             | FloatType     | false
        "[real]"                                             | new MSSQLDatabase()   | "[real]"                                             | FloatType     | false
        "smalldatetime"                                      | new MSSQLDatabase()   | "[smalldatetime]"                                    | DateTimeType  | false
        "[smalldatetime]"                                    | new MSSQLDatabase()   | "[smalldatetime]"                                    | DateTimeType  | false
        "smallint"                                           | new MSSQLDatabase()   | "[smallint]"                                         | SmallIntType  | false
        "[smallint]"                                         | new MSSQLDatabase()   | "[smallint]"                                         | SmallIntType  | false
        "smallmoney"                                         | new MSSQLDatabase()   | "[smallmoney]"                                       | CurrencyType  | false
        "[smallmoney]"                                       | new MSSQLDatabase()   | "[smallmoney]"                                       | CurrencyType  | false
        "text"                                               | new MSSQLDatabase()   | "[text]"                                             | ClobType      | false
        "[text]"                                             | new MSSQLDatabase()   | "[text]"                                             | ClobType      | false
        "time"                                               | new MSSQLDatabase()   | "[time](7)"                                          | TimeType      | false
        "[time]"                                             | new MSSQLDatabase()   | "[time](7)"                                          | TimeType      | false
        "time(6)"                                            | new MSSQLDatabase()   | "[time](6)"                                          | TimeType      | false
        "[time](6)"                                          | new MSSQLDatabase()   | "[time](6)"                                          | TimeType      | false
        "timestamp"                                          | new MSSQLDatabase()   | "[datetime]"                                         | TimestampType | false
        "tinyint"                                            | new MSSQLDatabase()   | "[tinyint]"                                          | TinyIntType   | false
        "[tinyint]"                                          | new MSSQLDatabase()   | "[tinyint]"                                          | TinyIntType   | false
        "uniqueidentifier"                                   | new MSSQLDatabase()   | "[uniqueidentifier]"                                 | UUIDType      | false
        "[uniqueidentifier]"                                 | new MSSQLDatabase()   | "[uniqueidentifier]"                                 | UUIDType      | false
        "uuid"                                               | new MSSQLDatabase()   | "[uniqueidentifier]"                                 | UUIDType      | false
        "varbinary"                                          | new MSSQLDatabase()   | "[varbinary](1)"                                     | BlobType      | false
        "[varbinary]"                                        | new MSSQLDatabase()   | "[varbinary](1)"                                     | BlobType      | false
        "varbinary(8000)"                                    | new MSSQLDatabase()   | "[varbinary](8000)"                                  | BlobType      | false
        "[varbinary](8000)"                                  | new MSSQLDatabase()   | "[varbinary](8000)"                                  | BlobType      | false
        "varbinary(MAX)"                                     | new MSSQLDatabase()   | "[varbinary](MAX)"                                   | BlobType      | false
        "[varbinary](MAX)"                                   | new MSSQLDatabase()   | "[varbinary](MAX)"                                   | BlobType      | false
        "varchar"                                            | new MSSQLDatabase()   | "[varchar](1)"                                       | VarcharType   | false
        "[varchar]"                                          | new MSSQLDatabase()   | "[varchar](1)"                                       | VarcharType   | false
        "varchar(8000)"                                      | new MSSQLDatabase()   | "[varchar](8000)"                                    | VarcharType   | false
        "[varchar](8000)"                                    | new MSSQLDatabase()   | "[varchar](8000)"                                    | VarcharType   | false
        "varchar(MAX)"                                       | new MSSQLDatabase()   | "[varchar](MAX)"                                     | VarcharType   | false
        "[varchar](MAX)"                                     | new MSSQLDatabase()   | "[varchar](MAX)"                                     | VarcharType   | false
        "xml"                                                | new MSSQLDatabase()   | "[xml]"                                              | UnknownType   | false
        "[xml]"                                              | new MSSQLDatabase()   | "[xml]"                                              | UnknownType   | false
        "xml(CONTENT)"                                       | new MSSQLDatabase()   | "[xml](CONTENT)"                                     | UnknownType   | false
        "[xml](CONTENT)"                                     | new MSSQLDatabase()   | "[xml](CONTENT)"                                     | UnknownType   | false
        "xml(DOCUMENT)"                                      | new MSSQLDatabase()   | "[xml](DOCUMENT)"                                    | UnknownType   | false
        "[xml](DOCUMENT)"                                    | new MSSQLDatabase()   | "[xml](DOCUMENT)"                                    | UnknownType   | false
        "xml([MySchema].[MyXmlSchemaCollection])"            | new MSSQLDatabase()   | "[xml]([MySchema].[MyXmlSchemaCollection])"          | UnknownType   | false
        "[xml]([MySchema].[MyXmlSchemaCollection])"          | new MSSQLDatabase()   | "[xml]([MySchema].[MyXmlSchemaCollection])"          | UnknownType   | false
        "xml(CONTENT [MySchema].[MyXmlSchemaCollection])"    | new MSSQLDatabase()   | "[xml](CONTENT [MySchema].[MyXmlSchemaCollection])"  | UnknownType   | false
        "[xml](CONTENT [MySchema].[MyXmlSchemaCollection])"  | new MSSQLDatabase()   | "[xml](CONTENT [MySchema].[MyXmlSchemaCollection])"  | UnknownType   | false
        "xml(DOCUMENT [MySchema].[MyXmlSchemaCollection])"   | new MSSQLDatabase()   | "[xml](DOCUMENT [MySchema].[MyXmlSchemaCollection])" | UnknownType   | false
        "[xml](DOCUMENT [MySchema].[MyXmlSchemaCollection])" | new MSSQLDatabase()   | "[xml](DOCUMENT [MySchema].[MyXmlSchemaCollection])" | UnknownType   | false
        "MySchema.MyUDT"                                     | new MSSQLDatabase()   | "[MySchema].[MyUDT]"                                 | UnknownType   | false
        "MySchema.[MyUDT]"                                   | new MSSQLDatabase()   | "[MySchema].[MyUDT]"                                 | UnknownType   | false
        "[MySchema].MyUDT"                                   | new MSSQLDatabase()   | "[MySchema].[MyUDT]"                                 | UnknownType   | false
        "[MySchema].[MyUDT]"                                 | new MSSQLDatabase()   | "[MySchema].[MyUDT]"                                 | UnknownType   | false
    }
}
