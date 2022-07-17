package liquibase.snapshot.jvm

import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.snapshot.CachedRow
import liquibase.statement.DatabaseFunction
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

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
}
