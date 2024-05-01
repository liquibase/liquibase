package liquibase.database

import spock.lang.Specification

class BigQueryDatabaseTest extends Specification {

    def checkSetup() {
        when:
        def db = new BigQueryDatabase()

        then:
        db.getShortName() == "bigquery"
    }
}
