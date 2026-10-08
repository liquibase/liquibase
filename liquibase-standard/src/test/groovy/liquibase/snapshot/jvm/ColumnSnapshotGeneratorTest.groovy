package liquibase.snapshot.jvm

import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.snapshot.CachedRow
import liquibase.statement.DatabaseFunction
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Types

class ColumnSnapshotGeneratorTest extends Specification {
    private ColumnSnapshotGenerator columnSnapshotGenerator

    void setup() {
        columnSnapshotGenerator = new ColumnSnapshotGenerator()
    }

    // Regression test for LB-2110
    def "ReadDataType does not specify COLUMN_SIZE for postgres Arrays"() {
        given:
        def columnMetadata = new HashMap<String, Object>()
        columnMetadata.put("TYPE_NAME", "_numeric")
        columnMetadata.put("DATA_TYPE", 2003)
        columnMetadata.put("COLUMN_SIZE", 131089)

        when:
        def dataType = columnSnapshotGenerator
                .readDataType(new CachedRow(columnMetadata), new Column(), new PostgresDatabase())

        then:
        dataType.getColumnSize() == null
        dataType.getTypeName() == "numeric[]"
    }

    def "ReadDataType specifies column size for modifiable data types"() {
        given:
        def columnMetadata = new HashMap<String, Object>()
        columnMetadata.put("TYPE_NAME", "varchar")
        columnMetadata.put("DATA_TYPE", 12)
        columnMetadata.put("COLUMN_SIZE", 100)

        when:
        def dataType = columnSnapshotGenerator
                .readDataType(new CachedRow(columnMetadata), new Column(), new PostgresDatabase())

        then:
        dataType.getColumnSize() == 100
        dataType.getTypeName() == "varchar"
    }

    def "ReadDataType specifies column size for mysql datetime data fsp"() {
        given:
        def columnMetadata = new HashMap<String, Object>()
        columnMetadata.put("TYPE_NAME", "datetime")
        columnMetadata.put("DATA_TYPE", 93)
        columnMetadata.put("COLUMN_SIZE", 25)

        when:
        def dataType = columnSnapshotGenerator
                .readDataType(new CachedRow(columnMetadata), new Column(), new MySQLDatabase())

        then:
        dataType.getColumnSize() == 5
        dataType.getTypeName() == "datetime"
    }

    @Unroll
    def "readDefaultValue"() {
        expect:
        columnSnapshotGenerator.readDefaultValue(new CachedRow(["COLUMN_DEF": columnValue]), new Column("col").setType(new DataType(datatype)), db) == expected

        where:
        columnValue          | datatype  | db                     | expected
        null                 | "varchar" | new MSSQLDatabase()    | null
        "(NULL)"             | "varchar" | new MSSQLDatabase()    | new DatabaseFunction("null")
        "3"                  | "int"     | new PostgresDatabase() | 3
        3                    | "int"     | new PostgresDatabase() | 3
        "(3)::real"          | "float"   | new PostgresDatabase() | 3
        "3::real"            | "float"   | new PostgresDatabase() | 3
        "'a value'::varchar" | "varchar" | new PostgresDatabase() | "a value"
    }

    @Unroll
    def "read PostgreSQL real default #columnValue from JDBC metadata"() {
        given:
        def database = new PostgresDatabase()
        def column = new Column("col")
        def metadata = new CachedRow([
                "TYPE_NAME": "float4",
                "DATA_TYPE": Types.REAL,
                "COLUMN_SIZE": 8,
                "COLUMN_DEF": columnValue
        ])
        column.setType(columnSnapshotGenerator.readDataType(metadata, column, database))

        expect:
        columnSnapshotGenerator.readDefaultValue(metadata, column, database) == expected

        where:
        columnValue             | expected
        null                    | null
        "3"                     | new BigDecimal("3")
        "0.0"                   | new BigDecimal("0.0")
        "(3)::real"             | new BigDecimal("3")
        "'-1'::integer"         | new BigDecimal("-1")
        "(0.0)::real"           | new DatabaseFunction("(0.0)::real")
        "((1 + 2))::real"       | new DatabaseFunction("((1 + 2))::real")
        "('-1'::integer)::real" | new DatabaseFunction("('-1'::integer)::real")
        "random()"              | new DatabaseFunction("random()")
    }
}
