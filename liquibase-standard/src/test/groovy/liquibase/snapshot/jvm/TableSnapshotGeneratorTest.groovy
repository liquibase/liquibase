package liquibase.snapshot.jvm

import liquibase.database.Database
import liquibase.database.core.CockroachDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.snapshot.CachedRow
import liquibase.snapshot.JdbcDatabaseSnapshot
import spock.lang.Specification
import spock.lang.Unroll

class TableSnapshotGeneratorTest extends Specification {

    def "readTable picks up PARTITION_BY enrichment column when database is PostgresDatabase"() {
        given:
        def row = new CachedRow([
                "TABLE_NAME"         : "test_tbl_part",
                "TABLE_SCHEM"        : "public",
                "TABLE_CAT"          : "lbtest",
                "PARTITION_BY"       : "RANGE (test_date_int)",
                "REMARKS"            : null,
                "TABLESPACE_NAME"    : null,
                "DEFAULT_TABLESPACE" : null,
                "TEMPORARY"          : null,
        ] as Map<String, Object>)
        def generator = new TableSnapshotGenerator()
        def database = new PostgresDatabase()

        when:
        def table = generator.readTable(row, database)

        then:
        table.getName() == "test_tbl_part"
        table.getPartitionBy() == "RANGE (test_date_int)"
    }

    def "readTable does not set partitionBy when PARTITION_BY column is absent"() {
        given:
        def row = new CachedRow([
                "TABLE_NAME"         : "plain_tbl",
                "TABLE_SCHEM"        : "public",
                "TABLE_CAT"          : "lbtest",
                "REMARKS"            : null,
                "TABLESPACE_NAME"    : null,
                "DEFAULT_TABLESPACE" : null,
                "TEMPORARY"          : null,
        ] as Map<String, Object>)
        def generator = new TableSnapshotGenerator()
        def database = new PostgresDatabase()

        when:
        def table = generator.readTable(row, database)

        then:
        table.getName() == "plain_tbl"
        table.getPartitionBy() == null
    }

    def "readTable ignores PARTITION_BY column on non-Postgres databases (safety guard)"() {
        given:
        // A stray PARTITION_BY column on a CachedRow from a non-Postgres path must not
        // accidentally end up on the Table model. This guards against schema-name collisions
        // with future enrichment paths in other dialects.
        def row = new CachedRow([
                "TABLE_NAME"         : "test_tbl",
                "TABLE_SCHEM"        : "public",
                "TABLE_CAT"          : "lbtest",
                "PARTITION_BY"       : "RANGE (col)",
                "REMARKS"            : null,
                "TABLESPACE_NAME"    : null,
                "DEFAULT_TABLESPACE" : null,
                "TEMPORARY"          : null,
        ] as Map<String, Object>)
        def generator = new TableSnapshotGenerator()
        def database = new MySQLDatabase()

        when:
        def table = generator.readTable(row, database)

        then:
        table.getPartitionBy() == null
    }

    @Unroll
    def "supportsPartitionKeyCatalog is #expected for #desc"() {
        expect:
        JdbcDatabaseSnapshot.supportsPartitionKeyCatalog(database) == expected

        where:
        desc                        | database                     || expected
        "PostgreSQL 10"             | postgresWithVersion(10)      || true
        "PostgreSQL 15"             | postgresWithVersion(15)      || true
        "PostgreSQL 9 (pre-10)"     | postgresWithVersion(9)       || false
        "Redshift-like PG 8.0.2"    | postgresWithVersion(8)       || false
        "CockroachDB"               | new CockroachDatabase()      || false
        "non-Postgres (MySQL)"      | new MySQLDatabase()          || false
    }

    // RedshiftDatabase lives in an extension off core's classpath, so a PostgresDatabase pinned to major
    // version 8 stands in for it — the guard only sees `instanceof PostgresDatabase` and the reported version.
    private static Database postgresWithVersion(int major) {
        new PostgresDatabase() {
            @Override
            int getDatabaseMajorVersion() { major }
        }
    }
}
