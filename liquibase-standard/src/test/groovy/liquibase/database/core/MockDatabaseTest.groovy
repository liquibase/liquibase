package liquibase.database.core

import liquibase.structure.core.Catalog
import liquibase.structure.core.Table
import spock.lang.Specification

class MockDatabaseTest extends Specification {

    def correctObjectName() {
        expect:
        def database = new MockDatabase();
        database.correctObjectName("lowercase", Catalog.class) == "lowercasE"
        database.correctObjectName("UPPERCASE", Catalog.class) == "uppercasE"
        database.correctObjectName("MixEdCasE", Catalog.class) == "mixedcasE"
    }

    def supportsCreateIfNotExist() {
        given:
        def database = new MockDatabase()

        expect:
        verifyAll {
            !database.supportsCreateIfNotExists(Table.class)
        }
    }
}
