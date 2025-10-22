package liquibase.datatype

import liquibase.database.core.*
import liquibase.datatype.core.*
import spock.lang.Specification
import spock.lang.Unroll

class DataTypeFactoryTest extends Specification {

    @Unroll("#featureName: #liquibaseString for #database")
    void fromDescription() throws Exception {
        when:
        def liquibaseType = DataTypeFactory.getInstance().fromDescription(liquibaseString, database)
        def databaseType = liquibaseType.toDatabaseDataType(database)
        def autoIncrement = liquibaseType.metaClass.respondsTo(liquibaseType, "isAutoIncrement") && liquibaseType.isAutoIncrement()

        then:
        databaseString == databaseType.toString()
        expectedType == liquibaseType.getClass()
        expectedAutoIncrement == autoIncrement
        expectedAdditionalInformation == liquibaseType.getAdditionalInformation()

        where:
        liquibaseString                                | database               | databaseString                                 | expectedType  | expectedAutoIncrement | expectedAdditionalInformation
        "int"                                          | new MockDatabase()     | "INT"                                          | IntType       | false                 | null
        "varchar(255)"                                 | new MockDatabase()     | "VARCHAR(255)"                                 | VarcharType   | false                 | null
        " varchar(255) "                               | new MockDatabase()     | "VARCHAR(255)"                                 | VarcharType   | false                 | null
        "int{autoIncrement:true}"                      | new MockDatabase()     | "INT"                                          | IntType       | true                  | null
        "int{autoIncrement:false}"                     | new MockDatabase()     | "INT"                                          | IntType       | false                 | null
        "int{}"                                        | new MockDatabase()     | "INT"                                          | IntType       | false                 | null
        "character varying(256)"                       | new MockDatabase()     | "VARCHAR(256)"                                 | VarcharType   | false                 | null
        "serial8"                                      | new MockDatabase()     | "BIGINT"                                       | BigIntType    | true                  | null
        "int4"                                         | new MockDatabase()     | "INT"                                          | IntType       | false                 | null
        "serial4"                                      | new MockDatabase()     | "INT"                                          | IntType       | true                  | null
        "xml"                                          | new MockDatabase()     | "XML"                                          | XMLType       | false                 | null
        "real"                                         | new DB2Database()      | "REAL"                                         | FloatType     | false                 | null
        "varbinary(200)"                               | new DB2Database()      | "VARBINARY(200)"                               | BlobType      | false                 | null
        "binary(200)"                                  | new DB2Database()      | "BINARY(200)"                                  | BlobType      | false                 | null
        "java.sql.Types.VARBINARY(200)"                | new DB2Database()      | "VARBINARY(200)"                               | BlobType      | false                 | null
        "xml"                                          | new DB2Database()      | "XML"                                          | XMLType       | false                 | null
        "bigint"                                       | new MSSQLDatabase()    | "bigint"                                       | BigIntType    | false                 | null
        "[bigint]"                                     | new MSSQLDatabase()    | "bigint"                                       | BigIntType    | false                 | null
        "binary"                                       | new MSSQLDatabase()    | "binary(1)"                                    | BlobType      | false                 | null
        "[binary]"                                     | new MSSQLDatabase()    | "binary(1)"                                    | BlobType      | false                 | null
        "binary(8000)"                                 | new MSSQLDatabase()    | "binary(8000)"                                 | BlobType      | false                 | null
        "[binary](8000)"                               | new MSSQLDatabase()    | "binary(8000)"                                 | BlobType      | false                 | null
        "bit"                                          | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false                 | null
        "[bit]"                                        | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false                 | null
        "blob"                                         | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false                 | null
        "boolean"                                      | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false                 | null
        "char"                                         | new MSSQLDatabase()    | "char(1)"                                      | CharType      | false                 | null
        "[char]"                                       | new MSSQLDatabase()    | "char(1)"                                      | CharType      | false                 | null
        "char(8000)"                                   | new MSSQLDatabase()    | "char(8000)"                                   | CharType      | false                 | null
        "[char](8000)"                                 | new MSSQLDatabase()    | "char(8000)"                                   | CharType      | false                 | null
        "clob"                                         | new MSSQLDatabase()    | "varchar(MAX)"                                 | ClobType      | false                 | null
        "currency"                                     | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false                 | null
        "date"                                         | new MSSQLDatabase()    | "date"                                         | DateType      | false                 | null
        "[date]"                                       | new MSSQLDatabase()    | "date"                                         | DateType      | false                 | null
        "datetime"                                     | new MSSQLDatabase()    | "datetime"                                     | DateTimeType  | false                 | null
        "[datetime]"                                   | new MSSQLDatabase()    | "datetime"                                     | DateTimeType  | false                 | null
        "datetime2"                                    | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false                 | null
        "[datetime2]"                                  | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false                 | null
        "datetime2(6)"                                 | new MSSQLDatabase()    | "datetime2(6)"                                 | DateTimeType  | false                 | null
        "[datetime2](6)"                               | new MSSQLDatabase()    | "datetime2(6)"                                 | DateTimeType  | false                 | null
        "datetime2(7)"                                 | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false                 | null
        "[datetime2](7)"                               | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false                 | null
        "datetimeoffset"                               | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false                 | null
        "[datetimeoffset]"                             | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false                 | null
        "datetimeoffset(6)"                            | new MSSQLDatabase()    | "datetimeoffset(6)"                            | UnknownType   | false                 | null
        "[datetimeoffset](6)"                          | new MSSQLDatabase()    | "datetimeoffset(6)"                            | UnknownType   | false                 | null
        "datetimeoffset(7)"                            | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false                 | null
        "[datetimeoffset](7)"                          | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false                 | null
        "decimal"                                      | new MSSQLDatabase()    | "decimal(18, 0)"                               | DecimalType   | false                 | null
        "[decimal]"                                    | new MSSQLDatabase()    | "decimal(18, 0)"                               | DecimalType   | false                 | null
        "decimal(19)"                                  | new MSSQLDatabase()    | "decimal(19, 0)"                               | DecimalType   | false                 | null
        "[decimal](19)"                                | new MSSQLDatabase()    | "decimal(19, 0)"                               | DecimalType   | false                 | null
        "decimal(19, 2)"                               | new MSSQLDatabase()    | "decimal(19, 2)"                               | DecimalType   | false                 | null
        "[decimal](19, 2)"                             | new MSSQLDatabase()    | "decimal(19, 2)"                               | DecimalType   | false                 | null
        "double"                                       | new MSSQLDatabase()    | "float(53)"                                    | DoubleType    | false                 | null
        "float"                                        | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false                 | null
        "[float]"                                      | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false                 | null
        "float(53)"                                    | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false                 | null
        "[float](53)"                                  | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false                 | null
        "geography"                                    | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false                 | null
        "[geography]"                                  | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false                 | null
        "geography(1, 2)"                              | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false                 | null
        "geometry"                                     | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false                 | null
        "[geometry]"                                   | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false                 | null
        "geometry(3, 4)"                               | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false                 | null
        "image"                                        | new MSSQLDatabase()    | "image"                                        | BlobType      | false                 | null
        "[image]"                                      | new MSSQLDatabase()    | "image"                                        | BlobType      | false                 | null
        "int"                                          | new MSSQLDatabase()    | "int"                                          | IntType       | false                 | null
        "[int]"                                        | new MSSQLDatabase()    | "int"                                          | IntType       | false                 | null
        "integer"                                      | new MSSQLDatabase()    | "int"                                          | IntType       | false                 | null
        "mediumint"                                    | new MSSQLDatabase()    | "int"                                          | MediumIntType | false                 | null
        "money"                                        | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false                 | null
        "[money]"                                      | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false                 | null
        "nchar"                                        | new MSSQLDatabase()    | "nchar(1)"                                     | NCharType     | false                 | null
        "[nchar]"                                      | new MSSQLDatabase()    | "nchar(1)"                                     | NCharType     | false                 | null
        "nchar(4000)"                                  | new MSSQLDatabase()    | "nchar(4000)"                                  | NCharType     | false                 | null
        "[nchar](4000)"                                | new MSSQLDatabase()    | "nchar(4000)"                                  | NCharType     | false                 | null
        "nclob"                                        | new MSSQLDatabase()    | "nvarchar(MAX)"                                | ClobType      | false                 | null
        "ntext"                                        | new MSSQLDatabase()    | "nvarchar (max)"                               | ClobType      | false                 | null
        "[ntext]"                                      | new MSSQLDatabase()    | "nvarchar (max)"                               | ClobType      | false                 | null
        "number"                                       | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false                 | null
        "numeric"                                      | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false                 | null
        "[numeric]"                                    | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false                 | null
        "numeric(19)"                                  | new MSSQLDatabase()    | "numeric(19, 0)"                               | NumberType    | false                 | null
        "[numeric](19)"                                | new MSSQLDatabase()    | "numeric(19, 0)"                               | NumberType    | false                 | null
        "numeric(19, 2)"                               | new MSSQLDatabase()    | "numeric(19, 2)"                               | NumberType    | false                 | null
        "[numeric](19, 2)"                             | new MSSQLDatabase()    | "numeric(19, 2)"                               | NumberType    | false                 | null
        "nvarchar"                                     | new MSSQLDatabase()    | "nvarchar(1)"                                  | NVarcharType  | false                 | null
        "[nvarchar]"                                   | new MSSQLDatabase()    | "nvarchar(1)"                                  | NVarcharType  | false                 | null
        "nvarchar(4000)"                               | new MSSQLDatabase()    | "nvarchar(4000)"                               | NVarcharType  | false                 | null
        "[nvarchar](4000)"                             | new MSSQLDatabase()    | "nvarchar(4000)"                               | NVarcharType  | false                 | null
        "nvarchar(MAX)"                                | new MSSQLDatabase()    | "nvarchar(MAX)"                                | NVarcharType  | false                 | null
        "[nvarchar](MAX)"                              | new MSSQLDatabase()    | "nvarchar(MAX)"                                | NVarcharType  | false                 | null
        "real"                                         | new MSSQLDatabase()    | "real"                                         | FloatType     | false                 | null
        "[real]"                                       | new MSSQLDatabase()    | "real"                                         | FloatType     | false                 | null
        "smalldatetime"                                | new MSSQLDatabase()    | "smalldatetime"                                | DateTimeType  | false                 | null
        "[smalldatetime]"                              | new MSSQLDatabase()    | "smalldatetime"                                | DateTimeType  | false                 | null
        "smallint"                                     | new MSSQLDatabase()    | "smallint"                                     | SmallIntType  | false                 | null
        "[smallint]"                                   | new MSSQLDatabase()    | "smallint"                                     | SmallIntType  | false                 | null
        "smallmoney"                                   | new MSSQLDatabase()    | "smallmoney"                                   | CurrencyType  | false                 | null
        "[smallmoney]"                                 | new MSSQLDatabase()    | "smallmoney"                                   | CurrencyType  | false                 | null
        "sql_variant"                                  | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false                 | null
        "[sql_variant]"                                | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false                 | null
        "sql_variant(5, 6)"                            | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false                 | null
        "text"                                         | new MSSQLDatabase()    | "varchar (max)"                                | ClobType      | false                 | null
        "[text]"                                       | new MSSQLDatabase()    | "varchar (max)"                                | ClobType      | false                 | null
        "time"                                         | new MSSQLDatabase()    | "time"                                         | TimeType      | false                 | null
        "[time]"                                       | new MSSQLDatabase()    | "time"                                         | TimeType      | false                 | null
        "time(6)"                                      | new MSSQLDatabase()    | "time(6)"                                      | TimeType      | false                 | null
        "[time](6)"                                    | new MSSQLDatabase()    | "time(6)"                                      | TimeType      | false                 | null
        "time(7)"                                      | new MSSQLDatabase()    | "time"                                         | TimeType      | false                 | null
        "[time](7)"                                    | new MSSQLDatabase()    | "time"                                         | TimeType      | false                 | null
        "timestamp"                                    | new MSSQLDatabase()    | "datetime2"                                    | TimestampType | false                 | null
        "tinyint"                                      | new MSSQLDatabase()    | "tinyint"                                      | TinyIntType   | false                 | null
        "[tinyint]"                                    | new MSSQLDatabase()    | "tinyint"                                      | TinyIntType   | false                 | null
        "uniqueidentifier"                             | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false                 | null
        "[uniqueidentifier]"                           | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false                 | null
        "uuid"                                         | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false                 | null
        "varbinary"                                    | new MSSQLDatabase()    | "varbinary(1)"                                 | BlobType      | false                 | null
        "[varbinary]"                                  | new MSSQLDatabase()    | "varbinary(1)"                                 | BlobType      | false                 | null
        "varbinary(8000)"                              | new MSSQLDatabase()    | "varbinary(8000)"                              | BlobType      | false                 | null
        "[varbinary](8000)"                            | new MSSQLDatabase()    | "varbinary(8000)"                              | BlobType      | false                 | null
        "varbinary(MAX)"                               | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false                 | null
        "[varbinary](MAX)"                             | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false                 | null
        "varchar"                                      | new MSSQLDatabase()    | "varchar(1)"                                   | VarcharType   | false                 | null
        "[varchar]"                                    | new MSSQLDatabase()    | "varchar(1)"                                   | VarcharType   | false                 | null
        "varchar(8000)"                                | new MSSQLDatabase()    | "varchar(8000)"                                | VarcharType   | false                 | null
        "[varchar](8000)"                              | new MSSQLDatabase()    | "varchar(8000)"                                | VarcharType   | false                 | null
        "varchar(MAX)"                                 | new MSSQLDatabase()    | "varchar(MAX)"                                 | VarcharType   | false                 | null
        "[varchar](MAX)"                               | new MSSQLDatabase()    | "varchar(MAX)"                                 | VarcharType   | false                 | null
        "xml"                                          | new MSSQLDatabase()    | "xml"                                          | XMLType       | false                 | null
        "[xml]"                                        | new MSSQLDatabase()    | "xml"                                          | XMLType       | false                 | null
        "xml(CONTENT)"                                 | new MSSQLDatabase()    | "xml(CONTENT)"                                 | XMLType       | false                 | null
        "[xml](CONTENT)"                               | new MSSQLDatabase()    | "xml(CONTENT)"                                 | XMLType       | false                 | null
        "xml(DOCUMENT)"                                | new MSSQLDatabase()    | "xml(DOCUMENT)"                                | XMLType       | false                 | null
        "[xml](DOCUMENT)"                              | new MSSQLDatabase()    | "xml(DOCUMENT)"                                | XMLType       | false                 | null
        "xml(MySchema.MyXmlSchemaCollection)"          | new MSSQLDatabase()    | "xml(MySchema.MyXmlSchemaCollection)"          | XMLType       | false                 | null
        "xml(MySchema.MyXmlSchemaCollection)"          | new MSSQLDatabase()    | "xml(MySchema.MyXmlSchemaCollection)"          | XMLType       | false                 | null
        "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | new MSSQLDatabase()    | "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | XMLType       | false                 | null
        "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | new MSSQLDatabase()    | "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | XMLType       | false                 | null
        "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | new MSSQLDatabase()    | "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | XMLType       | false                 | null
        "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | new MSSQLDatabase()    | "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | XMLType       | false                 | null
        "MySchema.MyUDT"                               | new MSSQLDatabase()    | "MySchema.MyUDT"                               | UnknownType   | false                 | null
        "MySchema.[MyUDT]"                             | new MSSQLDatabase()    | "MySchema.[MyUDT]"                             | UnknownType   | false                 | null
        "[MySchema].MyUDT"                             | new MSSQLDatabase()    | "MySchema.MyUDT"                               | UnknownType   | false                 | null
        "[MySchema].[MyUDT]"                           | new MSSQLDatabase()    | "[MySchema].[MyUDT]"                           | UnknownType   | false                 | null
        "char COLLATE Latin1_General_BIN"              | new MSSQLDatabase()    | "char(1) COLLATE Latin1_General_BIN"           | CharType      | false                 | "COLLATE Latin1_General_BIN"
        "[char] COLLATE Latin1_General_BIN"            | new MSSQLDatabase()    | "char(1) COLLATE Latin1_General_BIN"           | CharType      | false                 | "COLLATE Latin1_General_BIN"
        "char(255) COLLATE Latin1_General_BIN"         | new MSSQLDatabase()    | "char(255) COLLATE Latin1_General_BIN"         | CharType      | false                 | "COLLATE Latin1_General_BIN"
        "[char](255) COLLATE Latin1_General_BIN"       | new MSSQLDatabase()    | "char(255) COLLATE Latin1_General_BIN"         | CharType      | false                 | "COLLATE Latin1_General_BIN"
        "nchar COLLATE Latin1_General_BIN"             | new MSSQLDatabase()    | "nchar(1) COLLATE Latin1_General_BIN"          | NCharType     | false                 | "COLLATE Latin1_General_BIN"
        "[nchar] COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "nchar(1) COLLATE Latin1_General_BIN"          | NCharType     | false                 | "COLLATE Latin1_General_BIN"
        "nchar(255) COLLATE Latin1_General_BIN"        | new MSSQLDatabase()    | "nchar(255) COLLATE Latin1_General_BIN"        | NCharType     | false                 | "COLLATE Latin1_General_BIN"
        "[nchar](255) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "nchar(255) COLLATE Latin1_General_BIN"        | NCharType     | false                 | "COLLATE Latin1_General_BIN"
        "ntext COLLATE Latin1_General_BIN"             | new MSSQLDatabase()    | "nvarchar (max) COLLATE Latin1_General_BIN"    | ClobType      | false                 | "COLLATE Latin1_General_BIN"
        "[ntext] COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "nvarchar (max) COLLATE Latin1_General_BIN"    | ClobType      | false                 | "COLLATE Latin1_General_BIN"
        "nvarchar COLLATE Latin1_General_BIN"          | new MSSQLDatabase()    | "nvarchar(1) COLLATE Latin1_General_BIN"       | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "[nvarchar] COLLATE Latin1_General_BIN"        | new MSSQLDatabase()    | "nvarchar(1) COLLATE Latin1_General_BIN"       | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "nvarchar(255) COLLATE Latin1_General_BIN"     | new MSSQLDatabase()    | "nvarchar(255) COLLATE Latin1_General_BIN"     | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "[nvarchar](255) COLLATE Latin1_General_BIN"   | new MSSQLDatabase()    | "nvarchar(255) COLLATE Latin1_General_BIN"     | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "nvarchar(MAX) COLLATE Latin1_General_BIN"     | new MSSQLDatabase()    | "nvarchar(MAX) COLLATE Latin1_General_BIN"     | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "[nvarchar](MAX) COLLATE Latin1_General_BIN"   | new MSSQLDatabase()    | "nvarchar(MAX) COLLATE Latin1_General_BIN"     | NVarcharType  | false                 | "COLLATE Latin1_General_BIN"
        "text COLLATE Latin1_General_BIN"              | new MSSQLDatabase()    | "varchar (max) COLLATE Latin1_General_BIN"     | ClobType      | false                 | "COLLATE Latin1_General_BIN"
        "[text] COLLATE Latin1_General_BIN"            | new MSSQLDatabase()    | "varchar (max) COLLATE Latin1_General_BIN"     | ClobType      | false                 | "COLLATE Latin1_General_BIN"
        "varchar COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "varchar(1) COLLATE Latin1_General_BIN"        | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "[varchar] COLLATE Latin1_General_BIN"         | new MSSQLDatabase()    | "varchar(1) COLLATE Latin1_General_BIN"        | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "varchar(255) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "varchar(255) COLLATE Latin1_General_BIN"      | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "[varchar](255) COLLATE Latin1_General_BIN"    | new MSSQLDatabase()    | "varchar(255) COLLATE Latin1_General_BIN"      | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "varchar(MAX) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "varchar(MAX) COLLATE Latin1_General_BIN"      | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "[varchar](MAX) COLLATE Latin1_General_BIN"    | new MSSQLDatabase()    | "varchar(MAX) COLLATE Latin1_General_BIN"      | VarcharType   | false                 | "COLLATE Latin1_General_BIN"
        "VARCHAR(20 CHAR)"                             | new MSSQLDatabase()    | "varchar(20)"                                  | VarcharType   | false                 | null
        "varchar(20 char)"                             | new MSSQLDatabase()    | "varchar(20)"                                  | VarcharType   | false                 | null
        "CHAR(20 CHAR)"                                | new MSSQLDatabase()    | "char(20)"                                     | CharType      | false                 | null
        "INT"                                          | new MySQLDatabase()    | "INT"                                          | IntType       | false                 | null
        "INT UNSIGNED"                                 | new MySQLDatabase()    | "INT UNSIGNED"                                 | IntType       | false                 | "UNSIGNED"
        "INT(11) UNSIGNED"                             | new MySQLDatabase()    | "INT UNSIGNED"                                 | IntType       | false                 | "UNSIGNED"
        "TINYINT"                                      | new MySQLDatabase()    | "TINYINT"                                      | TinyIntType   | false                 | null
        "TINYINT UNSIGNED"                             | new MySQLDatabase()    | "TINYINT UNSIGNED"                             | TinyIntType   | false                 | "UNSIGNED"
        "TINYINT(1) UNSIGNED"                          | new MySQLDatabase()    | "TINYINT(1) UNSIGNED"                          | TinyIntType   | false                 | "UNSIGNED"
        "SMALLINT"                                     | new MySQLDatabase()    | "SMALLINT"                                     | SmallIntType  | false                 | null
        "SMALLINT UNSIGNED"                            | new MySQLDatabase()    | "SMALLINT UNSIGNED"                            | SmallIntType  | false                 | "UNSIGNED"
        "MEDIUMINT"                                    | new MySQLDatabase()    | "MEDIUMINT"                                    | MediumIntType | false                 | null
        "MEDIUMINT UNSIGNED"                           | new MySQLDatabase()    | "MEDIUMINT UNSIGNED"                           | MediumIntType | false                 | "UNSIGNED"
        "BIGINT"                                       | new MySQLDatabase()    | "BIGINT"                                       | BigIntType    | false                 | null
        "BIGINT UNSIGNED"                              | new MySQLDatabase()    | "BIGINT UNSIGNED"                              | BigIntType    | false                 | "UNSIGNED"
        "BINARY(16)"                                   | new MySQLDatabase()    | "BINARY(16)"                                   | BlobType      | false                 | null
        "tinyblob"                                     | new MySQLDatabase()    | "TINYBLOB"                                     | BlobType      | false                 | null
        "tinytext"                                     | new MySQLDatabase()    | "TINYTEXT"                                     | ClobType      | false                 | null
        "mediumblob"                                   | new MySQLDatabase()    | "MEDIUMBLOB"                                   | BlobType      | false                 | null
        "mediumtext"                                   | new MySQLDatabase()    | "MEDIUMTEXT"                                   | ClobType      | false                 | null
        "real"                                         | new MySQLDatabase()    | "REAL"                                         | FloatType     | false                 | null
        "nclob"                                        | new OracleDatabase()   | "NCLOB"                                        | ClobType      | false                 | null
        "xml"                                          | new OracleDatabase()   | "XMLTYPE"                                      | XMLType       | false                 | null
        "xmltype"                                      | new OracleDatabase()   | "XMLTYPE"                                      | XMLType       | false                 | null
        "timestamp"                                    | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false                 | null
        "timestamp(6)"                                 | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "TIMESTAMP WITH TIMEZONE"                      | new OracleDatabase()   | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | "WITH TIMEZONE"
        "TIMESTAMP(6) WITH TIMEZONE"                   | new OracleDatabase()   | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | "WITH TIMEZONE"
        "timestamp without timezone"                   | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false                 | "without timezone"
        "timestamp(6) without timezone"                | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false                 | "without timezone"
        "timestamptz"                                  | new OracleDatabase()   | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | null
        "timestamptz(6)"                               | new OracleDatabase()   | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | null
        "java.sql.Timestamp"                           | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false                 | null
        "java.sql.Timestamp(6)"                        | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP"                     | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP(6)"                  | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new OracleDatabase()   | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new OracleDatabase()   | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | null
        "xml"                                          | new PostgresDatabase() | "XML"                                          | XMLType       | false                 | null
        "timestamp"                                    | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false                 | null
        "timestamp(6)"                                 | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false                 | null
        "timestamp with timezone"                      | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | "with timezone"
        "timestamp(6) with timezone"                   | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | "with timezone"
        "timestamp without timezone"                   | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false                 | "without timezone"
        "timestamp(6) without timezone"                | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false                 | "without timezone"
        "timestamptz"                                  | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | "WITH TIME ZONE"
        "timestamptz(6)"                               | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | "WITH TIME ZONE"
        "java.sql.Timestamp"                           | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false                 | null
        "java.sql.Timestamp(6)"                        | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP"                     | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP(6)"                  | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | null
        "BINARY(16)"                                   | new H2Database()       | "BINARY(16)"                                   | BlobType      | false                 | null
        "timestamp"                                    | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false                 | null
        "timestamp(6)"                                 | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "TIMESTAMP WITH TIMEZONE"                      | new H2Database()       | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | "WITH TIMEZONE"
        "TIMESTAMP(6) WITH TIMEZONE"                   | new H2Database()       | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | "WITH TIMEZONE"
        "timestamp without timezone"                   | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false                 | "without timezone"
        "timestamp(6) without timezone"                | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false                 | "without timezone"
        "timestamptz"                                  | new H2Database()       | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | null
        "timestamptz(6)"                               | new H2Database()       | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | null
        "java.sql.Timestamp"                           | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false                 | null
        "java.sql.Timestamp(6)"                        | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP"                     | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP(6)"                  | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new H2Database()       | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false                 | null
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new H2Database()       | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false                 | null
        "\${invalidParam}"                             | new H2Database()       | "\${INVALIDPARAM}"                             | UnknownType   | false                 | null
        "currency"                                     | new H2Database()       | "DECIMAL(18, 4)"                               | CurrencyType  | false                 | null
        "BOOLEAN"                                      | new H2Database()       | "BOOLEAN"                                      | BooleanType   | false                 | null
        "BOOLEAN(1)"                                   | new H2Database()       | "BOOLEAN"                                      | BooleanType   | false                 | null
        "BOOLEAN(0)"                                   | new H2Database()       | "BOOLEAN"                                      | BooleanType   | false                 | null
        "BOOLEAN(10)"                                  | new H2Database()       | "BOOLEAN"                                      | BooleanType   | false                 | null
        "INT(20)"                                      | new SybaseDatabase()   | "INT"                                          | IntType       | false                 | null
        "SMALLINT(20)"                                 | new SybaseDatabase()   | "SMALLINT"                                     | SmallIntType  | false                 | null
        "TINYINT(20)"                                  | new SybaseDatabase()   | "TINYINT"                                      | TinyIntType   | false                 | null
        "long binary"                                  | new SybaseDatabase()   | "IMAGE"                                        | BlobType      | false                 | null
        "long varbinary"                               | new SybaseDatabase()   | "IMAGE"                                        | BlobType      | false                 | null
        "long varchar"                                 | new SybaseDatabase()   | "TEXT"                                         | ClobType      | false                 | null
        "long nvarchar"                                | new SybaseDatabase()   | "TEXT"                                         | ClobType      | false                 | null
        "character varying"                            | new SybaseDatabase()   | "VARCHAR"                                      | VarcharType   | false                 | null
        "uuid"                                         | new MariaDBDatabase()  | "UUID"                                         | UUIDType      | false                 | null
    }

    @Unroll("#featureName: #object for #database")
    void fromObject() throws Exception {
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
