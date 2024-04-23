package liquibase.database

import spock.lang.Specification

class BigqueryDatabaseTest extends Specification {

    def checkSetup() {
        when:
        def db = new BigqueryDatabase()

        then:
        db.getShortName() == "bigquery"
    }
}
