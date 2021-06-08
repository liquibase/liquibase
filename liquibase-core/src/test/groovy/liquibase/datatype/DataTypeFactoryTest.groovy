package liquibase.datatype

import liquibase.database.core.*
import liquibase.datatype.core.*
import liquibase.database.core.MockDatabase
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

        where:
        liquibaseString                                | database               | databaseString                                 | expectedType  | expectedAutoIncrement
        "int"                                          | new MockDatabase()     | "INT"                                          | IntType       | false
        "varchar(255)"                                 | new MockDatabase()     | "VARCHAR(255)"                                 | VarcharType   | false
        " varchar(255) "                               | new MockDatabase()     | "VARCHAR(255)"                                 | VarcharType   | false
        "int{autoIncrement:true}"                      | new MockDatabase()     | "INT"                                          | IntType       | true
        "int{autoIncrement:false}"                     | new MockDatabase()     | "INT"                                          | IntType       | false
        "int{}"                                        | new MockDatabase()     | "INT"                                          | IntType       | false
        "character varying(256)"                       | new MockDatabase()     | "VARCHAR(256)"                                 | VarcharType   | false
        "serial8"                                      | new MockDatabase()     | "BIGINT"                                       | BigIntType    | true
        "int4"                                         | new MockDatabase()     | "INT"                                          | IntType       | false
        "serial4"                                      | new MockDatabase()     | "INT"                                          | IntType       | true
        "xml"                                          | new MockDatabase()     | "XML"                                          | XMLType       | false
        "real"                                         | new DB2Database()      | "REAL"                                         | FloatType     | false
        "xml"                                          | new DB2Database()      | "XML"                                          | XMLType       | false
        "bigint"                                       | new MSSQLDatabase()    | "bigint"                                       | BigIntType    | false
        "[bigint]"                                     | new MSSQLDatabase()    | "bigint"                                       | BigIntType    | false
        "binary"                                       | new MSSQLDatabase()    | "binary(1)"                                    | BlobType      | false
        "[binary]"                                     | new MSSQLDatabase()    | "binary(1)"                                    | BlobType      | false
        "binary(8000)"                                 | new MSSQLDatabase()    | "binary(8000)"                                 | BlobType      | false
        "[binary](8000)"                               | new MSSQLDatabase()    | "binary(8000)"                                 | BlobType      | false
        "bit"                                          | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false
        "[bit]"                                        | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false
        "blob"                                         | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false
        "boolean"                                      | new MSSQLDatabase()    | "bit"                                          | BooleanType   | false
        "char"                                         | new MSSQLDatabase()    | "char(1)"                                      | CharType      | false
        "[char]"                                       | new MSSQLDatabase()    | "char(1)"                                      | CharType      | false
        "char(8000)"                                   | new MSSQLDatabase()    | "char(8000)"                                   | CharType      | false
        "[char](8000)"                                 | new MSSQLDatabase()    | "char(8000)"                                   | CharType      | false
        "clob"                                         | new MSSQLDatabase()    | "varchar(MAX)"                                 | ClobType      | false
        "currency"                                     | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false
        "date"                                         | new MSSQLDatabase()    | "date"                                         | DateType      | false
        "[date]"                                       | new MSSQLDatabase()    | "date"                                         | DateType      | false
        "datetime"                                     | new MSSQLDatabase()    | "datetime"                                     | DateTimeType  | false
        "[datetime]"                                   | new MSSQLDatabase()    | "datetime"                                     | DateTimeType  | false
        "datetime2"                                    | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false
        "[datetime2]"                                  | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false
        "datetime2(6)"                                 | new MSSQLDatabase()    | "datetime2(6)"                                 | DateTimeType  | false
        "[datetime2](6)"                               | new MSSQLDatabase()    | "datetime2(6)"                                 | DateTimeType  | false
        "datetime2(7)"                                 | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false
        "[datetime2](7)"                               | new MSSQLDatabase()    | "datetime2"                                    | DateTimeType  | false
        "datetimeoffset"                               | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false
        "[datetimeoffset]"                             | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false
        "datetimeoffset(6)"                            | new MSSQLDatabase()    | "datetimeoffset(6)"                            | UnknownType   | false
        "[datetimeoffset](6)"                          | new MSSQLDatabase()    | "datetimeoffset(6)"                            | UnknownType   | false
        "datetimeoffset(7)"                            | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false
        "[datetimeoffset](7)"                          | new MSSQLDatabase()    | "datetimeoffset"                               | UnknownType   | false
        "decimal"                                      | new MSSQLDatabase()    | "decimal(18, 0)"                               | DecimalType   | false
        "[decimal]"                                    | new MSSQLDatabase()    | "decimal(18, 0)"                               | DecimalType   | false
        "decimal(19)"                                  | new MSSQLDatabase()    | "decimal(19, 0)"                               | DecimalType   | false
        "[decimal](19)"                                | new MSSQLDatabase()    | "decimal(19, 0)"                               | DecimalType   | false
        "decimal(19, 2)"                               | new MSSQLDatabase()    | "decimal(19, 2)"                               | DecimalType   | false
        "[decimal](19, 2)"                             | new MSSQLDatabase()    | "decimal(19, 2)"                               | DecimalType   | false
        "double"                                       | new MSSQLDatabase()    | "float(53)"                                    | DoubleType    | false
        "float"                                        | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false
        "[float]"                                      | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false
        "float(53)"                                    | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false
        "[float](53)"                                  | new MSSQLDatabase()    | "float(53)"                                    | FloatType     | false
        "geography"                                    | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false
        "[geography]"                                  | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false
        "geography(1, 2)"                              | new MSSQLDatabase()    | "geography"                                    | UnknownType   | false
        "geometry"                                     | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false
        "[geometry]"                                   | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false
        "geometry(3, 4)"                               | new MSSQLDatabase()    | "geometry"                                     | UnknownType   | false
        "image"                                        | new MSSQLDatabase()    | "image"                                        | BlobType      | false
        "[image]"                                      | new MSSQLDatabase()    | "image"                                        | BlobType      | false
        "int"                                          | new MSSQLDatabase()    | "int"                                          | IntType       | false
        "[int]"                                        | new MSSQLDatabase()    | "int"                                          | IntType       | false
        "integer"                                      | new MSSQLDatabase()    | "int"                                          | IntType       | false
        "mediumint"                                    | new MSSQLDatabase()    | "int"                                          | MediumIntType | false
        "money"                                        | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false
        "[money]"                                      | new MSSQLDatabase()    | "money"                                        | CurrencyType  | false
        "nchar"                                        | new MSSQLDatabase()    | "nchar(1)"                                     | NCharType     | false
        "[nchar]"                                      | new MSSQLDatabase()    | "nchar(1)"                                     | NCharType     | false
        "nchar(4000)"                                  | new MSSQLDatabase()    | "nchar(4000)"                                  | NCharType     | false
        "[nchar](4000)"                                | new MSSQLDatabase()    | "nchar(4000)"                                  | NCharType     | false
        "nclob"                                        | new MSSQLDatabase()    | "nvarchar(MAX)"                                | ClobType      | false
        "ntext"                                        | new MSSQLDatabase()    | "nvarchar (max)"                               | ClobType      | false
        "[ntext]"                                      | new MSSQLDatabase()    | "nvarchar (max)"                               | ClobType      | false
        "number"                                       | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false
        "numeric"                                      | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false
        "[numeric]"                                    | new MSSQLDatabase()    | "numeric(18, 0)"                               | NumberType    | false
        "numeric(19)"                                  | new MSSQLDatabase()    | "numeric(19, 0)"                               | NumberType    | false
        "[numeric](19)"                                | new MSSQLDatabase()    | "numeric(19, 0)"                               | NumberType    | false
        "numeric(19, 2)"                               | new MSSQLDatabase()    | "numeric(19, 2)"                               | NumberType    | false
        "[numeric](19, 2)"                             | new MSSQLDatabase()    | "numeric(19, 2)"                               | NumberType    | false
        "nvarchar"                                     | new MSSQLDatabase()    | "nvarchar(1)"                                  | NVarcharType  | false
        "[nvarchar]"                                   | new MSSQLDatabase()    | "nvarchar(1)"                                  | NVarcharType  | false
        "nvarchar(4000)"                               | new MSSQLDatabase()    | "nvarchar(4000)"                               | NVarcharType  | false
        "[nvarchar](4000)"                             | new MSSQLDatabase()    | "nvarchar(4000)"                               | NVarcharType  | false
        "nvarchar(MAX)"                                | new MSSQLDatabase()    | "nvarchar(MAX)"                                | NVarcharType  | false
        "[nvarchar](MAX)"                              | new MSSQLDatabase()    | "nvarchar(MAX)"                                | NVarcharType  | false
        "real"                                         | new MSSQLDatabase()    | "real"                                         | FloatType     | false
        "[real]"                                       | new MSSQLDatabase()    | "real"                                         | FloatType     | false
        "smalldatetime"                                | new MSSQLDatabase()    | "smalldatetime"                                | DateTimeType  | false
        "[smalldatetime]"                              | new MSSQLDatabase()    | "smalldatetime"                                | DateTimeType  | false
        "smallint"                                     | new MSSQLDatabase()    | "smallint"                                     | SmallIntType  | false
        "[smallint]"                                   | new MSSQLDatabase()    | "smallint"                                     | SmallIntType  | false
        "smallmoney"                                   | new MSSQLDatabase()    | "smallmoney"                                   | CurrencyType  | false
        "[smallmoney]"                                 | new MSSQLDatabase()    | "smallmoney"                                   | CurrencyType  | false
        "sql_variant"                                  | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false
        "[sql_variant]"                                | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false
        "sql_variant(5, 6)"                            | new MSSQLDatabase()    | "sql_variant"                                  | UnknownType   | false
        "text"                                         | new MSSQLDatabase()    | "varchar (max)"                                | ClobType      | false
        "[text]"                                       | new MSSQLDatabase()    | "varchar (max)"                                | ClobType      | false
        "time"                                         | new MSSQLDatabase()    | "time"                                         | TimeType      | false
        "[time]"                                       | new MSSQLDatabase()    | "time"                                         | TimeType      | false
        "time(6)"                                      | new MSSQLDatabase()    | "time(6)"                                      | TimeType      | false
        "[time](6)"                                    | new MSSQLDatabase()    | "time(6)"                                      | TimeType      | false
        "time(7)"                                      | new MSSQLDatabase()    | "time"                                         | TimeType      | false
        "[time](7)"                                    | new MSSQLDatabase()    | "time"                                         | TimeType      | false
        "timestamp"                                    | new MSSQLDatabase()    | "datetime2"                                    | TimestampType | false
        "tinyint"                                      | new MSSQLDatabase()    | "tinyint"                                      | TinyIntType   | false
        "[tinyint]"                                    | new MSSQLDatabase()    | "tinyint"                                      | TinyIntType   | false
        "uniqueidentifier"                             | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false
        "[uniqueidentifier]"                           | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false
        "uuid"                                         | new MSSQLDatabase()    | "uniqueidentifier"                             | UUIDType      | false
        "varbinary"                                    | new MSSQLDatabase()    | "varbinary(1)"                                 | BlobType      | false
        "[varbinary]"                                  | new MSSQLDatabase()    | "varbinary(1)"                                 | BlobType      | false
        "varbinary(8000)"                              | new MSSQLDatabase()    | "varbinary(8000)"                              | BlobType      | false
        "[varbinary](8000)"                            | new MSSQLDatabase()    | "varbinary(8000)"                              | BlobType      | false
        "varbinary(MAX)"                               | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false
        "[varbinary](MAX)"                             | new MSSQLDatabase()    | "varbinary(MAX)"                               | BlobType      | false
        "varchar"                                      | new MSSQLDatabase()    | "varchar(1)"                                   | VarcharType   | false
        "[varchar]"                                    | new MSSQLDatabase()    | "varchar(1)"                                   | VarcharType   | false
        "varchar(8000)"                                | new MSSQLDatabase()    | "varchar(8000)"                                | VarcharType   | false
        "[varchar](8000)"                              | new MSSQLDatabase()    | "varchar(8000)"                                | VarcharType   | false
        "varchar(MAX)"                                 | new MSSQLDatabase()    | "varchar(MAX)"                                 | VarcharType   | false
        "[varchar](MAX)"                               | new MSSQLDatabase()    | "varchar(MAX)"                                 | VarcharType   | false
        "xml"                                          | new MSSQLDatabase()    | "xml"                                          | XMLType       | false
        "[xml]"                                        | new MSSQLDatabase()    | "xml"                                          | XMLType       | false
        "xml(CONTENT)"                                 | new MSSQLDatabase()    | "xml(CONTENT)"                                 | XMLType       | false
        "[xml](CONTENT)"                               | new MSSQLDatabase()    | "xml(CONTENT)"                                 | XMLType       | false
        "xml(DOCUMENT)"                                | new MSSQLDatabase()    | "xml(DOCUMENT)"                                | XMLType       | false
        "[xml](DOCUMENT)"                              | new MSSQLDatabase()    | "xml(DOCUMENT)"                                | XMLType       | false
        "xml(MySchema.MyXmlSchemaCollection)"          | new MSSQLDatabase()    | "xml(MySchema.MyXmlSchemaCollection)"          | XMLType       | false
        "xml(MySchema.MyXmlSchemaCollection)"          | new MSSQLDatabase()    | "xml(MySchema.MyXmlSchemaCollection)"          | XMLType       | false
        "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | new MSSQLDatabase()    | "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | XMLType       | false
        "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | new MSSQLDatabase()    | "xml(CONTENT MySchema.MyXmlSchemaCollection)"  | XMLType       | false
        "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | new MSSQLDatabase()    | "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | XMLType       | false
        "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | new MSSQLDatabase()    | "xml(DOCUMENT MySchema.MyXmlSchemaCollection)" | XMLType       | false
        "MySchema.MyUDT"                               | new MSSQLDatabase()    | "MySchema.MyUDT"                               | UnknownType   | false
        "MySchema.[MyUDT]"                             | new MSSQLDatabase()    | "MySchema.[MyUDT]"                             | UnknownType   | false
        "[MySchema].MyUDT"                             | new MSSQLDatabase()    | "MySchema.MyUDT"                               | UnknownType   | false
        "[MySchema].[MyUDT]"                           | new MSSQLDatabase()    | "[MySchema].[MyUDT]"                           | UnknownType   | false
        "char COLLATE Latin1_General_BIN"              | new MSSQLDatabase()    | "char(1) COLLATE Latin1_General_BIN"           | CharType      | false
        "[char] COLLATE Latin1_General_BIN"            | new MSSQLDatabase()    | "char(1) COLLATE Latin1_General_BIN"           | CharType      | false
        "char(255) COLLATE Latin1_General_BIN"         | new MSSQLDatabase()    | "char(255) COLLATE Latin1_General_BIN"         | CharType      | false
        "[char](255) COLLATE Latin1_General_BIN"       | new MSSQLDatabase()    | "char(255) COLLATE Latin1_General_BIN"         | CharType      | false
        "nchar COLLATE Latin1_General_BIN"             | new MSSQLDatabase()    | "nchar(1) COLLATE Latin1_General_BIN"          | NCharType     | false
        "[nchar] COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "nchar(1) COLLATE Latin1_General_BIN"          | NCharType     | false
        "nchar(255) COLLATE Latin1_General_BIN"        | new MSSQLDatabase()    | "nchar(255) COLLATE Latin1_General_BIN"        | NCharType     | false
        "[nchar](255) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "nchar(255) COLLATE Latin1_General_BIN"        | NCharType     | false
        "ntext COLLATE Latin1_General_BIN"             | new MSSQLDatabase()    | "nvarchar (max) COLLATE Latin1_General_BIN"    | ClobType      | false
        "[ntext] COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "nvarchar (max) COLLATE Latin1_General_BIN"    | ClobType      | false
        "nvarchar COLLATE Latin1_General_BIN"          | new MSSQLDatabase()    | "nvarchar(1) COLLATE Latin1_General_BIN"       | NVarcharType  | false
        "[nvarchar] COLLATE Latin1_General_BIN"        | new MSSQLDatabase()    | "nvarchar(1) COLLATE Latin1_General_BIN"       | NVarcharType  | false
        "nvarchar(255) COLLATE Latin1_General_BIN"     | new MSSQLDatabase()    | "nvarchar(255) COLLATE Latin1_General_BIN"     | NVarcharType  | false
        "[nvarchar](255) COLLATE Latin1_General_BIN"   | new MSSQLDatabase()    | "nvarchar(255) COLLATE Latin1_General_BIN"     | NVarcharType  | false
        "nvarchar(MAX) COLLATE Latin1_General_BIN"     | new MSSQLDatabase()    | "nvarchar(MAX) COLLATE Latin1_General_BIN"     | NVarcharType  | false
        "[nvarchar](MAX) COLLATE Latin1_General_BIN"   | new MSSQLDatabase()    | "nvarchar(MAX) COLLATE Latin1_General_BIN"     | NVarcharType  | false
        "text COLLATE Latin1_General_BIN"              | new MSSQLDatabase()    | "varchar (max) COLLATE Latin1_General_BIN"     | ClobType      | false
        "[text] COLLATE Latin1_General_BIN"            | new MSSQLDatabase()    | "varchar (max) COLLATE Latin1_General_BIN"     | ClobType      | false
        "varchar COLLATE Latin1_General_BIN"           | new MSSQLDatabase()    | "varchar(1) COLLATE Latin1_General_BIN"        | VarcharType   | false
        "[varchar] COLLATE Latin1_General_BIN"         | new MSSQLDatabase()    | "varchar(1) COLLATE Latin1_General_BIN"        | VarcharType   | false
        "varchar(255) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "varchar(255) COLLATE Latin1_General_BIN"      | VarcharType   | false
        "[varchar](255) COLLATE Latin1_General_BIN"    | new MSSQLDatabase()    | "varchar(255) COLLATE Latin1_General_BIN"      | VarcharType   | false
        "varchar(MAX) COLLATE Latin1_General_BIN"      | new MSSQLDatabase()    | "varchar(MAX) COLLATE Latin1_General_BIN"      | VarcharType   | false
        "[varchar](MAX) COLLATE Latin1_General_BIN"    | new MSSQLDatabase()    | "varchar(MAX) COLLATE Latin1_General_BIN"      | VarcharType   | false
        "INT"                                          | new MySQLDatabase()    | "INT"                                          | IntType       | false
        "INT UNSIGNED"                                 | new MySQLDatabase()    | "INT UNSIGNED"                                 | IntType       | false
        "INT(11) UNSIGNED"                             | new MySQLDatabase()    | "INT UNSIGNED"                                 | IntType       | false
        "TINYINT"                                      | new MySQLDatabase()    | "TINYINT"                                      | TinyIntType   | false
        "TINYINT UNSIGNED"                             | new MySQLDatabase()    | "TINYINT UNSIGNED"                             | TinyIntType   | false
        "TINYINT(1) UNSIGNED"                          | new MySQLDatabase()    | "TINYINT(1) UNSIGNED"                          | TinyIntType   | false
        "SMALLINT"                                     | new MySQLDatabase()    | "SMALLINT"                                     | SmallIntType  | false
        "SMALLINT UNSIGNED"                            | new MySQLDatabase()    | "SMALLINT UNSIGNED"                            | SmallIntType  | false
        "MEDIUMINT"                                    | new MySQLDatabase()    | "MEDIUMINT"                                    | MediumIntType | false
        "MEDIUMINT UNSIGNED"                           | new MySQLDatabase()    | "MEDIUMINT UNSIGNED"                           | MediumIntType | false
        "BIGINT"                                       | new MySQLDatabase()    | "BIGINT"                                       | BigIntType    | false
        "BIGINT UNSIGNED"                              | new MySQLDatabase()    | "BIGINT UNSIGNED"                              | BigIntType    | false
        "BINARY(16)"                                   | new MySQLDatabase()    | "BINARY(16)"                                   | BlobType      | false
        "tinyblob"                                     | new MySQLDatabase()    | "TINYBLOB"                                     | BlobType      | false
        "tinytext"                                     | new MySQLDatabase()    | "TINYTEXT"                                     | ClobType      | false
        "mediumblob"                                   | new MySQLDatabase()    | "MEDIUMBLOB"                                   | BlobType      | false
        "mediumtext"                                   | new MySQLDatabase()    | "MEDIUMTEXT"                                   | ClobType      | false
        "real"                                         | new MySQLDatabase()    | "REAL"                                         | FloatType     | false
        "nclob"                                        | new OracleDatabase()   | "NCLOB"                                        | ClobType      | false
        "xml"                                          | new OracleDatabase()   | "XMLTYPE"                                      | XMLType       | false
        "xmltype"                                      | new OracleDatabase()   | "XMLTYPE"                                      | XMLType       | false
        "timestamp"                                    | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false
        "timestamp(6)"                                 | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false
        "TIMESTAMP WITH TIMEZONE"                      | new OracleDatabase()   | "TIMESTAMP WITH TIMEZONE"                      | TimestampType | false
        "TIMESTAMP(6) WITH TIMEZONE"                   | new OracleDatabase()   | "TIMESTAMP(6) WITH TIMEZONE"                   | TimestampType | false
        "timestamp without timezone"                   | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false
        "timestamp(6) without timezone"                | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false
        "timestamptz"                                  | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false
        "timestamptz(6)"                               | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Timestamp"                           | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false
        "java.sql.Timestamp(6)"                        | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Types.TIMESTAMP"                     | new OracleDatabase()   | "TIMESTAMP"                                    | TimestampType | false
        "java.sql.Types.TIMESTAMP(6)"                  | new OracleDatabase()   | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new OracleDatabase()   | "TIMESTAMP WITH TIMEZONE"                      | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new OracleDatabase()   | "TIMESTAMP(6) WITH TIMEZONE"                   | TimestampType | false
        "xml"                                          | new PostgresDatabase() | "XML"                                          | XMLType       | false
        "timestamp"                                    | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false
        "timestamp(6)"                                 | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false
        "timestamp with timezone"                      | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false
        "timestamp(6) with timezone"                   | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false
        "timestamp without timezone"                   | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false
        "timestamp(6) without timezone"                | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false
        "timestamptz"                                  | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false
        "timestamptz(6)"                               | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false
        "java.sql.Timestamp"                           | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false
        "java.sql.Timestamp(6)"                        | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false
        "java.sql.Types.TIMESTAMP"                     | new PostgresDatabase() | "TIMESTAMP WITHOUT TIME ZONE"                  | TimestampType | false
        "java.sql.Types.TIMESTAMP(6)"                  | new PostgresDatabase() | "TIMESTAMP(6) WITHOUT TIME ZONE"               | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new PostgresDatabase() | "TIMESTAMP WITH TIME ZONE"                     | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new PostgresDatabase() | "TIMESTAMP(6) WITH TIME ZONE"                  | TimestampType | false
        "BINARY(16)"                                   | new H2Database()       | "BINARY(16)"                                   | BlobType      | false
        "timestamp"                                    | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false
        "timestamp(6)"                                 | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false
        "TIMESTAMP WITH TIMEZONE"                      | new H2Database()       | "TIMESTAMP WITH TIME ZONE"                      | TimestampType | false
        "TIMESTAMP(6) WITH TIMEZONE"                   | new H2Database()       | "TIMESTAMP(6) WITH TIME ZONE"                   | TimestampType | false
        "timestamp without timezone"                   | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false
        "timestamp(6) without timezone"                | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false
        "timestamptz"                                  | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false
        "timestamptz(6)"                               | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Timestamp"                           | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false
        "java.sql.Timestamp(6)"                        | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Types.TIMESTAMP"                     | new H2Database()       | "TIMESTAMP"                                    | TimestampType | false
        "java.sql.Types.TIMESTAMP(6)"                  | new H2Database()       | "TIMESTAMP(6)"                                 | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE"       | new H2Database()       | "TIMESTAMP WITH TIME ZONE"                      | TimestampType | false
        "java.sql.Types.TIMESTAMP_WITH_TIMEZONE(6)"    | new H2Database()       | "TIMESTAMP(6) WITH TIME ZONE"                   | TimestampType | false
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
