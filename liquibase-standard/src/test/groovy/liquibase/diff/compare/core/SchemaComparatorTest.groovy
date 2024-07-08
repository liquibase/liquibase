package liquibase.diff.compare.core

import liquibase.database.core.MockDatabase
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class SchemaComparatorTest extends Specification {

    def comparator = new SchemaComparator()

    def priority() {
        expect:
        comparator.getPriority(Schema.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_TYPE
        comparator.getPriority(Catalog.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
        comparator.getPriority(Table.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
    }

    def hashObject() {
        expect:
        comparator.hash(new Schema(), new MockDatabase(), null) == null
    }

    @Unroll("isSameObject: #object1 vs #object2 with default schema #defaultSchema")
    def "isSameObject"() {
        given:
        def databaseThatSupportsSchemas = new MockDatabase()
        databaseThatSupportsSchemas.setSupportsCatalogs(true)
        databaseThatSupportsSchemas.setSupportsSchemas(true)

        def databaseThatDoesNotSupportSchemas = new MockDatabase()
        databaseThatDoesNotSupportSchemas.setSupportsCatalogs(true)
        databaseThatDoesNotSupportSchemas.setSupportsSchemas(false)

        def databaseThatDoesNotSupportCatalogs = new MockDatabase()
        databaseThatDoesNotSupportCatalogs.setSupportsCatalogs(false)

        if (defaultSchema != null) {
            databaseThatSupportsSchemas.setDefaultSchemaName(defaultSchema)
            databaseThatDoesNotSupportSchemas.setDefaultSchemaName(defaultSchema)
        }

        expect:
        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, null, databaseThatSupportsSchemas) == isSameIfSupportsSchemas
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, null, databaseThatSupportsSchemas) == isSameIfSupportsSchemas

        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, null, databaseThatDoesNotSupportSchemas) == isSameIfNotSupportsSchemas
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, null, databaseThatDoesNotSupportSchemas) == isSameIfNotSupportsSchemas

        // always true if doesn't support catalogs
        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, null, databaseThatDoesNotSupportCatalogs) == true
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, null, databaseThatDoesNotSupportCatalogs) == true


        where:
        object1                                        | object2                                        | defaultSchema | isSameIfSupportsSchemas | isSameIfNotSupportsSchemas
        new Schema((String) null, null)                | new Schema((String) null, null)                | null          | true                    | true
        new Schema((String) null, null)                | new Schema((String) null, null)                | "MySchem"     | true                    | true
        new Schema((String) null, null)                | null                                           | null          | true                    | true
        new Schema((String) null, null)                | null                                           | "MySchem"     | true                    | true
        new Schema("Cat1", null)                       | new Schema("Cat1", null)                       | null          | true                    | true
        new Schema("Cat1", "Schem1")                   | new Schema("Cat1", "Schem1")                   | null          | true                    | true
        new Schema("Cat1", "Schem1")                   | new Schema("Cat1", "Schem2")                   | null          | false                   | true
        new Schema("Cat1", "Schem1")                   | new Schema("Cat1", "Schem1")                   | "MySchem"     | true                    | true
        new Schema("Cat1", "MySchem")                  | new Schema("Cat1", null)                       | "MySchem"     | true                    | true
        new Schema("Cat1", null)                       | new Schema("Cat1", "MySchem")                  | "MySchem"     | true                    | true
        new Schema("Cat1", "schem2")                   | new Schema("Cat1", null)                       | "MySchem"     | false                   | true
        new Schema("Cat1", "SCHEM1")                   | new Schema("Cat1", "schem1")                   | null          | true                    | true
        new Schema("Cat1", "schema1").setDefault(true) | new Schema("Cat1", "schema2").setDefault(true) | null          | true                    | true
        new Schema("Cat1", null)                       | new Schema("Cat1", "schema2").setDefault(true) | "schema2"     | true                    | true
        new Schema("Cat1", "schema2")                  | new Schema("Cat1", null).setDefault(true)      | "schema2"     | true                    | true
    }
}
