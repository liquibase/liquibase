package liquibase.diff.compare.core

import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.database.core.MockDatabase
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class CatalogComparatorTest extends Specification {

    def comparator = new CatalogComparator()

    def priority() {
        expect:
        comparator.getPriority(Catalog.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_TYPE
        comparator.getPriority(Schema.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
        comparator.getPriority(Table.class, new MockDatabase()) == DatabaseObjectComparator.PRIORITY_NONE
    }


    def hashObject() {
        expect:
        comparator.hash(new Catalog(), new MockDatabase(), null) == null
    }

    @Unroll("isSameObject: #object1 vs #object2 with default catalog #defaultCatalog")
    def "isSameObject"() {
        given:

        def databaseThatSupportCatalogs = new MockDatabase()
        databaseThatSupportCatalogs.setSupportsCatalogs(true)

        def databaseThatDoesNotSupportCatalogs = new MockDatabase()
        databaseThatDoesNotSupportCatalogs.setSupportsCatalogs(false)

        if (defaultCatalog != null) {
            databaseThatSupportCatalogs.setDefaultCatalogName(defaultCatalog)
            databaseThatDoesNotSupportCatalogs.setDefaultCatalogName(defaultCatalog)
        }

        expect:
        DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, null, databaseThatSupportCatalogs) == isSameIfSupportsCatalogs
        DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, null, databaseThatSupportCatalogs) == isSameIfSupportsCatalogs

        // always true if doesn't support catalogs
        assert DatabaseObjectComparatorFactory.instance.isSameObject(object1, object2, null, databaseThatDoesNotSupportCatalogs)
        assert DatabaseObjectComparatorFactory.instance.isSameObject(object2, object1, null, databaseThatDoesNotSupportCatalogs)


        where:
        object1                              | object2                              | defaultCatalog | isSameIfSupportsCatalogs
        new Catalog(null)                    | new Catalog(null)                    | null           | true
        new Catalog(null)                    | new Catalog(null)                    | "MyCat"        | true
        new Catalog(null)                    | null                                 | null           | true
        new Catalog(null)                    | null                                 | "MyCat"        | true
        new Catalog("Cat1")                  | new Catalog("Cat1")                  | null           | true
        new Catalog("Cat1")                  | new Catalog("Cat1")                  | "MyCat"        | true
        new Catalog("Cat1")                  | new Catalog("Cat2")                  | null           | false
        new Catalog("Cat1")                  | new Catalog("Cat2")                  | "Cat1"         | false
        new Catalog("Cat1")                  | new Catalog("Cat2")                  | "Cat2"         | false
        new Catalog("Cat1")                  | new Catalog(null)                    | "Cat1"         | true
        new Catalog(null)                    | new Catalog("Cat1")                  | "Cat1"         | true
        new Catalog("Cat1")                  | new Catalog("CAT1")                  | null           | true
        new Catalog("Cat1").setDefault(true) | new Catalog("Cat2").setDefault(true) | null           | true
        new Catalog("Cat1").setDefault(true) | new Catalog(null)                    | "Cat2"         | true
    }
}
