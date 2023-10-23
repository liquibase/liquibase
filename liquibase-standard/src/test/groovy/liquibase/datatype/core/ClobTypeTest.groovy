package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.statement.DatabaseFunction
import spock.lang.Specification
import spock.lang.Unroll

class ClobTypeTest extends Specification {
    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new ClobType()

        type.finishInitialization(origdef)


        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        origdef       | database               | expected
        ["TEXT(25500)"] | new MSSQLDatabase()    | "varchar (max)"
        ["Text"] | new MSSQLDatabase()    | "varchar (max)"
        ["[Text]"] | new MSSQLDatabase()    | "varchar (max)"
        ["NText"] | new MSSQLDatabase()    | "nvarchar (max)"
        ["[NText]"] | new MSSQLDatabase()    | "nvarchar (max)"
        ["TEXT COLLATE Latin1_General_BIN"] | new MSSQLDatabase()    | "varchar (max) COLLATE Latin1_General_BIN"
        ["nclob"] | new MSSQLDatabase()    | "nvarchar(MAX)"
        [""] | new MSSQLDatabase()    | "varchar(MAX)"
        [""] | new FirebirdDatabase()    | "BLOB SUB_TYPE TEXT"
        [""] | new SybaseASADatabase()    | "LONG VARCHAR"
        [""] | new SybaseASADatabase()    | "LONG VARCHAR"
        ["text"] | new MySQLDatabase()    | "TEXT"
        ["tinytext"] | new MySQLDatabase()    | "TINYTEXT"
        ["mediumtext"] | new MySQLDatabase()    | "MEDIUMTEXT"
        ["nclob"] | new MySQLDatabase()    | "LONGTEXT CHARACTER SET utf8"
        [""] | new MySQLDatabase()    | "LONGTEXT"
        ["longvarchar"] | new H2Database()    | "LONGVARCHAR"
        ["java.sql.Types.LONGVARCHAR"] | new H2Database()    | "LONGVARCHAR"
        ["longvarchar"] | new HsqlDatabase()    | "LONGVARCHAR"
        ["java.sql.Types.LONGVARCHAR"] | new HsqlDatabase()    | "LONGVARCHAR"
        [""] | new H2Database()    | "CLOB"
        [""] | new HsqlDatabase()    | "CLOB"
        [""] | new PostgresDatabase()    | "TEXT"
        [""] | new SQLiteDatabase()    | "TEXT"
        [""] | new SybaseDatabase()    | "TEXT"
        [""] | new OracleDatabase()    | "CLOB"
        ["nclob"] | new OracleDatabase()    | "NCLOB"
        ["text"] | new InformixDatabase()    | "TEXT"


    }

    @Unroll
    def "objectToSql"() {
        when:
        def type = new ClobType()

        then:
        type.objectToSql(object, database) == expected

        where:
        object          | database            | expected
        null            | new MockDatabase()  | null
        "'"            | new MockDatabase()  | "'"
        'text'            | new MockDatabase()  | /'text'/
        ""            | new MSSQLDatabase()  | /''/
        new DatabaseFunction("NEWID()")                         | new MockDatabase() | "NEWID()"
        new DatabaseFunction("NEWSEQUENTIALID()")               | new MockDatabase() | "NEWSEQUENTIALID()"
    }
}
