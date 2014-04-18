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
        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, databaseThatSupportsSchemas) == isSameIfSupportsSchemas
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, databaseThatSupportsSchemas) == isSameIfSupportsSchemas

        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, databaseThatDoesNotSupportSchemas) == isSameIfNotSupportsSchemas
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, databaseThatDoesNotSupportSchemas) == isSameIfNotSupportsSchemas

        // always true if doesn't support catalogs
        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, databaseThatDoesNotSupportCatalogs) == true
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, databaseThatDoesNotSupportCatalogs) == true


        where:
        object1 | object2 | defaultSchema | isSameIfSupportsSchemas | isSameIfNotSupportsSchemas
        new Schema((String) null, null) | new Schema((String) null, null) | null      | true  | true
        new Schema((String) null, null) | new Schema((String) null, null) | "MySchem" | true  | true
        new Schema((String) null, null) | null                            | null      | true  | true
        new Schema((String) null, null) | null                            | "MySchem" | true  | true
        new Schema("Cat1", null)        | new Schema("Cat1", null)        | null      | true  | true
        new Schema("Cat1", null)        | new Schema("Cat2", null)        | null      | false | false
        new Schema("Cat1", "Schem1")    | new Schema("Cat1", "Schem1")    | null      | true  | true
        new Schema("Cat1", "Schem1")    | new Schema("Cat1", "Schem2")    | null      | false | true
        new Schema("Cat1", "Schem1")    | new Schema("Cat1", "Schem1")    | "MySchem" | true  | true
        new Schema("Cat1", "MySchem")   | new Schema("Cat1", null)        | "MySchem" | true  | true
        //null matches default schema
        new Schema("Cat1", null) | new Schema("Cat1", "MySchem") | "MySchem" | true | true
        //null matches default schema
        new Schema("Cat1", "schem2") | new Schema("Cat1", null) | "MySchem" | false | true
    }

}
