package liquibase.snapshot.jvm

import liquibase.database.Database
import liquibase.database.core.OracleDatabase
import liquibase.database.core.PostgresDatabase
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawParameterizedSqlStatement
import liquibase.structure.core.Schema
import spock.lang.Specification
import spock.lang.Unroll

class SequenceSnapshotGeneratorTest extends Specification {

    private final static DEFAULT_CATALOG_NAME = "DEFAULT_CATALOG_NAME"
    private final static DEFAULT_SCHEMA_NAME = "public"

    @Unroll
    def "When catalog on changeset is #changesetCatalog, the SEQUENCE_OWNER will be #expectedSequenceOwner"() {
        given:
        SequenceSnapshotGenerator sequenceSnapshotGenerator = new SequenceSnapshotGenerator()
        Database database = Mock(OracleDatabase)
        database.getDefaultCatalogName() >> DEFAULT_CATALOG_NAME
        Schema schema = Mock(Schema)
        schema.getCatalogName() >> changesetCatalog

        when:
        SqlStatement sqlStatement = sequenceSnapshotGenerator.getSelectSequenceStatement(schema, database)
        String sql = ((RawParameterizedSqlStatement) sqlStatement).getSql()

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

    @Unroll
    def "When schema is '#schemaName', the return SQL schema name will be '#expectedSchemaName'"() {
        given:
        SequenceSnapshotGenerator sequenceSnapshotGenerator = new SequenceSnapshotGenerator()
        Database database = Mock(PostgresDatabase)
        database.getDefaultSchemaName() >> DEFAULT_SCHEMA_NAME

        when:
        Schema schema = Mock(Schema)
        schema.getName() >> schemaName

        then:
        SqlStatement sqlStatement = sequenceSnapshotGenerator.getSelectSequenceStatement(schema, database)
        String sql = sqlStatement.toString()
        sql.indexOf(expectedSchemaName) != -1

        where:
        schemaName | expectedSchemaName
        "mySchema"            | "mySchema"
        DEFAULT_SCHEMA_NAME   | DEFAULT_SCHEMA_NAME
        null                  | DEFAULT_SCHEMA_NAME
    }


}
