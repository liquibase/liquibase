package liquibase.serializer.core.string

import liquibase.database.core.MockDatabase
import liquibase.snapshot.EmptyDatabaseSnapshot
import liquibase.snapshot.MockDatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.structure.core.Column
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class StringSnapshotSerializerReadableTest extends Specification {

    def "serialized empty database"() throws Exception {
        given:
        def expectedString = "Database snapshot for jdbc://mock\n" +
                "-----------------------------------------------------------------\n" +
                "Database type: null\n" +
                "Database version: null\n" +
                "Database user: null\n" +
                "Included types:\n" +
                "    liquibase.structure.core.Catalog\n" +
                "    liquibase.structure.core.Column\n" +
                "    liquibase.structure.core.ForeignKey\n" +
                "    liquibase.structure.core.Index\n" +
                "    liquibase.structure.core.PrimaryKey\n" +
                "    liquibase.structure.core.Schema\n" +
                "    liquibase.structure.core.Sequence\n" +
                "    liquibase.structure.core.Table\n" +
                "    liquibase.structure.core.UniqueConstraint\n" +
                "    liquibase.structure.core.View\n"

        when:
        def emptyDatabase = new EmptyDatabaseSnapshot(new MockDatabase());

        then:
        new StringSnapshotSerializerReadable().serialize(emptyDatabase, false) == expectedString
    }


    @Unroll
    def "serialize mock database"() {
        when:
        def expectedString = "Database snapshot for jdbc://mock\n" +
                "-----------------------------------------------------------------\n" +
                "Database type: null\n" +
                "Database version: null\n" +
                "Database user: null\n" +
                "Included types:\n" +
                "    liquibase.structure.core.Catalog\n" +
                "    liquibase.structure.core.Column\n" +
                "    liquibase.structure.core.ForeignKey\n" +
                "    liquibase.structure.core.Index\n" +
                "    liquibase.structure.core.PrimaryKey\n" +
                "    liquibase.structure.core.Schema\n" +
                "    liquibase.structure.core.Sequence\n" +
                "    liquibase.structure.core.Table\n" +
                "    liquibase.structure.core.UniqueConstraint\n" +
                "    liquibase.structure.core.View\n" +
                "\n" +
                "Catalog & Schema: def_catalog / def_schema\n" +
                "    liquibase.structure.core.Table:\n" +
                "        def_table\n" +
                "            primaryKey: pk1\n" +
                "                columns: \n" +
                "                    id\n" +
                "                table: def_table"

        def database = new MockDatabase()


        def snapshotControl = new SnapshotControl(database)

        def mockDatabaseSnapshot = new MockDatabaseSnapshot(null, null, database, snapshotControl) {
            @Override
            public Set get(Class type) {
                def ret = new HashSet()
                if (type.isAssignableFrom(Schema.class)) {
                    ret.add(new Schema("def_catalog", "def_schema"))
                } else if (type.isAssignableFrom(Table.class)) {
                    def primarykey = new PrimaryKey("pk1", "def_catalog", "def_schema", "def_table",
                            new Column("id"))
                    def tablet = new Table("def_catalog", "def_schema", "def_table")
                    tablet.setPrimaryKey(primarykey)
                    ret.add(tablet)
                }
                return ret
            }
        }

        then:
        expectedString.trim() == new StringSnapshotSerializerReadable().serialize(mockDatabaseSnapshot, false).trim()
    }


}
