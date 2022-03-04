package liquibase.database.core

import spock.lang.Specification

class CockroachDatabaseTest extends Specification {

    def useSerialDatatypes() {
        when:
        def db = new CockroachDatabase()
        db.databaseMajorVersion = majorVersion
        db.databaseMinorVersion = minorVersion

        then:
        db.useSerialDatatypes() == expected

        where:
        majorVersion | minorVersion | expected
        20           | 0            | true
        20           | 1            | true
        20           | 2            | true
        21           | 0            | true
        21           | 1            | true
        21           | 2            | false
        21           | 3            | false
        22           | 0            | false
    }
}
