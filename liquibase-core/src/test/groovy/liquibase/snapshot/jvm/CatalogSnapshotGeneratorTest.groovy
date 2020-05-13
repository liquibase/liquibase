package liquibase.snapshot.jvm

import liquibase.database.Database
import liquibase.exception.DatabaseException
import liquibase.database.core.MockDatabase
import liquibase.snapshot.MockDatabaseSnapshot
import liquibase.structure.core.Catalog
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException

class CatalogSnapshotGeneratorTest extends Specification {

    @Unroll("#featureName: #example")
    def "snapshot catalog"() {
        when:
        def generator = new CatalogSnapshotGenerator() {
            @Override
            protected String[] getDatabaseCatalogNames(Database database) throws SQLException, DatabaseException {
                return ["catalog1", "catalog2", "def_catalog"]
            }
        }

        def database = new MockDatabase()
        database.setDefaultCatalogName("def_catalog")
        Catalog snapshot = (Catalog) generator.snapshotObject(example, new MockDatabaseSnapshot(null, null, database, null))

        then:
        snapshot.getName() == expectedName
        snapshot.isDefault() == isDefault

        where:
        example                         | expectedName | isDefault
        new Catalog("catalog1")    | "catalog1"    | false
        new Catalog("CATALOG1")    | "catalog1"    | false
        new Catalog(null)         | "def_catalog" | true
        new Catalog("def_catalog") | "def_catalog" | true


    }
}
