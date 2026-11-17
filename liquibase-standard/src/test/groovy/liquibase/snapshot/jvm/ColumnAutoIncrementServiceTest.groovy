package liquibase.snapshot.jvm

import spock.lang.Specification
import spock.lang.Unroll

class ColumnAutoIncrementServiceTest extends Specification {

    @Unroll
    def "PostgreSQL attidentity '#identityKind' is read as generation type #expected"() {
        given:
        Map<String, Object> row = [
                SCHEMA_NAME : "public",
                TABLE_NAME  : "t",
                COLUMN_NAME : "id",
                START_VALUE : 5L,
                INCREMENT_BY: 2L,
                IDENTITY_KIND: identityKind
        ]

        when:
        def info = ColumnAutoIncrementService.toAutoIncrementInformation(row)

        then:
        info.generationType == expected
        info.startWith == 5G.toBigInteger()
        info.incrementBy == 2G.toBigInteger()

        where:
        identityKind | expected
        "a"          | "ALWAYS"
        "d"          | "BY DEFAULT"
        ""           | null
        " "          | null
        null         | null
    }

    def "rows without an identity kind (other databases, PostgreSQL before 10) keep the generation type unset"() {
        when:
        def info = ColumnAutoIncrementService.toAutoIncrementInformation([START_VALUE: 1L, INCREMENT_BY: 1L])

        then:
        info.generationType == null
    }
}
