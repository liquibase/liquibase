package liquibase.snapshot.jvm

import liquibase.CatalogAndSchema
import liquibase.database.Database
import liquibase.exception.DatabaseException
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockDatabaseSnapshot
import liquibase.structure.core.Schema
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException

class SchemaSnapshotGeneratorTest extends Specification {

    @Unroll("#featureName: #example")
    def "snapshot schema"() {
        when:
        def generator = new SchemaSnapshotGenerator() {
            @Override
            protected String[] getDatabaseSchemaNames(Database database) throws SQLException, DatabaseException {
                return ["schema1", "schema2", "def_schema"]
            }

            @Override
            protected CatalogAndSchema toCatalogAndSchema(String tableSchema, Database database) {
                return new CatalogAndSchema(null, tableSchema).customize(database);
            }
        }


        def database = new MockDatabase()
        database.setDefaultSchemaName("def_schema")
        Schema snapshot = (Schema) generator.snapshotObject(example, new MockDatabaseSnapshot(null, null, database, null))

        then:
        snapshot.getName() == expectedName
        snapshot.isDefault() == isDefault

        where:
        example                         | expectedName | isDefault
        new Schema("cat", "schema1")    | "schema1"    | false
        new Schema("cat", "SCHEMA1")    | "schema1"    | false
        new Schema("cat", null)         | "def_schema" | true
        new Schema("cat", "def_schema") | "def_schema" | true


    }
}
