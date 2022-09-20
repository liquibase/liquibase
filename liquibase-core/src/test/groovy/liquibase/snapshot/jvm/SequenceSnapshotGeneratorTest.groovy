package liquibase.snapshot.jvm

import liquibase.database.Database
import liquibase.database.core.OracleDatabase
import liquibase.structure.core.Schema
import spock.lang.Specification
import spock.lang.Unroll

class SequenceSnapshotGeneratorTest extends Specification {

    private final static DEFAULT_CATALOG_NAME = "DEFAULT_CATALOG_NAME"

    @Unroll
    def "When catalog on changeset is #changesetCatalog, the SEQUENCE_OWNER will be #expectedSequenceOwner"() {
        given:
        SequenceSnapshotGenerator sequenceSnapshotGenerator = new SequenceSnapshotGenerator()
        Database database = Mock(OracleDatabase)
        database.getDefaultCatalogName() >> DEFAULT_CATALOG_NAME
        Schema schema = Mock(Schema)
        schema.getCatalogName() >> changesetCatalog

        when:
        String sql = sequenceSnapshotGenerator.getSelectSequenceSql(schema, database)

        then:
        String ownerEqualsClause = "SEQUENCE_OWNER = "
        String actualSequenceOwner = sql.substring(sql.indexOf(ownerEqualsClause) + ownerEqualsClause.size() + 1, sql.length() - 1)
        actualSequenceOwner == expectedSequenceOwner

        where:
        changesetCatalog | expectedSequenceOwner
        "ANY_STRING"     | "ANY_STRING"
        ""               | DEFAULT_CATALOG_NAME
        null             | DEFAULT_CATALOG_NAME
    }
}
