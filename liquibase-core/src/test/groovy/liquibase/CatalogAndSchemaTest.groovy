package liquibase

import liquibase.database.core.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class CatalogAndSchemaTest extends Specification {

    def databaseThatSupportsSchemas
    def databaseThatDoesNotSupportSchemas
    def databaseThatDoesNotSupportCatalogs

    def setup() {
        databaseThatSupportsSchemas = new MockDatabase()
        databaseThatSupportsSchemas.setSupportsCatalogs(true)
        databaseThatSupportsSchemas.setSupportsSchemas(true)

        databaseThatDoesNotSupportSchemas = new MockDatabase()
        databaseThatDoesNotSupportSchemas.setSupportsCatalogs(true)
        databaseThatDoesNotSupportSchemas.setSupportsSchemas(false)

        databaseThatDoesNotSupportCatalogs = new MockDatabase()
        databaseThatDoesNotSupportCatalogs.setSupportsCatalogs(false)
    }

    @Unroll("custom equals for #schema vs #comparisionSchema with default #defaultCatalogName/#defaultSchemaName")
    def "custom equals method"() {
        given:
        if (defaultCatalogName != null) {
            databaseThatSupportsSchemas.defaultCatalogName = defaultCatalogName
            databaseThatDoesNotSupportSchemas.defaultCatalogName = defaultCatalogName
        }
        if (defaultSchemaName != null) {
            databaseThatSupportsSchemas.defaultSchemaName = defaultSchemaName
        }

        expect:
        schema.equals(comparisionSchema, databaseThatSupportsSchemas) == expectIfSupportsSchemas
        schema.equals(comparisionSchema, databaseThatDoesNotSupportSchemas) == expectIfNotSupportsSchemas
        schema.equals(comparisionSchema, databaseThatDoesNotSupportCatalogs) == true

        where:
        schema | comparisionSchema | defaultCatalogName | defaultSchemaName | expectIfSupportsSchemas | expectIfNotSupportsSchemas
        new CatalogAndSchema(null, null)        | new CatalogAndSchema(null, null)        | null   | null      | true  | true
        new CatalogAndSchema("Cat1", null)      | new CatalogAndSchema("Cat1", null)      | null   | null      | true  | true
        new CatalogAndSchema("Cat1", "Schema1") | new CatalogAndSchema("Cat1", "Schema1") | null   | null      | true  | true
        new CatalogAndSchema(null, "Schema1")   | new CatalogAndSchema(null, "Schema1")   | null   | null      | true  | true
        new CatalogAndSchema("Cat1", "Schema1") | new CatalogAndSchema("Cat2", "Schema1") | null   | null      | false | false
        new CatalogAndSchema(null, "Schema1")   | new CatalogAndSchema(null, "Schema2")   | null   | null      | false | true
        new CatalogAndSchema("Cat1", "Schema1") | new CatalogAndSchema("Cat1", "Schema2") | null   | null      | false | true
        new CatalogAndSchema("CaT1", null)      | new CatalogAndSchema("cat1", null)      | null   | null      | true  | true
        new CatalogAndSchema(null, "SCHeMA1")   | new CatalogAndSchema(null, "schema1")   | null   | null      | true  | true
        new CatalogAndSchema(null, null)        | new CatalogAndSchema("cat1", null)      | "cat1" | "schema1" | true  | true
        new CatalogAndSchema(null, null)        | new CatalogAndSchema("cat1", "schema1") | "cat1" | "schema1" | true  | true
        new CatalogAndSchema(null, null)        | new CatalogAndSchema(null, "schema1")   | "cat1" | "schema1" | true  | true
        new CatalogAndSchema("cat1", null)      | new CatalogAndSchema(null, null)        | "cat1" | "schema1" | true  | true
        new CatalogAndSchema("cat1", "schema1") | new CatalogAndSchema(null, null)        | "cat1" | "schema1" | true  | true
        new CatalogAndSchema(null, "schema1")   | new CatalogAndSchema(null, null)        | "cat1" | "schema1" | true  | true
    }

    @Unroll("correct schema #schema.catalogName/#schema.schemaName default #defaultCatalogName/#defaultSchemaName")
    def correct() {
        given:
        if (defaultCatalogName != null) {
            databaseThatSupportsSchemas.defaultCatalogName = defaultCatalogName
            databaseThatDoesNotSupportSchemas.defaultCatalogName = defaultCatalogName
        }
        if (defaultSchemaName != null) {
            databaseThatSupportsSchemas.defaultSchemaName = defaultSchemaName
        }

        when:
        def correctedSupportsSchemas = schema.correct(databaseThatSupportsSchemas)
        def correctedNotSupportsSchemas = schema.correct(databaseThatDoesNotSupportSchemas)
        def correctedNotSupportsCatalogs = schema.correct(databaseThatDoesNotSupportCatalogs)

        then:
        "${correctedSupportsSchemas.catalogName}.${correctedSupportsSchemas.schemaName}" == expectIfSupportsSchemas
        "${correctedNotSupportsSchemas.catalogName}.${correctedNotSupportsSchemas.schemaName}" == expectIfNotSupportsSchemas
        "${correctedNotSupportsCatalogs.catalogName}.${correctedNotSupportsCatalogs.schemaName}" == "null.null"

        where:
        schema | defaultCatalogName | defaultSchemaName | expectIfSupportsSchemas | expectIfNotSupportsSchemas
        new CatalogAndSchema(null, null)        | null    | null       | "null.null"    | "null.null"
        new CatalogAndSchema(null, null)        | "MyCat" | null       | "null.null"    | "null.null"
        new CatalogAndSchema(null, null)        | null    | "MySchema" | "null.null"    | "null.null"
        new CatalogAndSchema(null, null)        | "MyCat" | "MySchema" | "null.null"    | "null.null"
        new CatalogAndSchema("Cat1", null)      | null    | null       | "Cat1.null"    | "Cat1.null"
        new CatalogAndSchema("Cat1", null)      | "Cat1"  | null       | "null.null"    | "null.null"
        new CatalogAndSchema("Cat1", "Schema1") | null    | null       | "Cat1.Schema1" | "Cat1.null"
        new CatalogAndSchema("Cat1", "Schema2") | null    | null       | "Cat1.Schema2" | "Cat1.null"
        new CatalogAndSchema("Cat1", "Schema1") | null    | "Schema1"  | "Cat1.null"    | "Cat1.null"
        new CatalogAndSchema("Cat1", "Schema1") | "Cat1"  | "Schema1"  | "null.null"    | "null.null"
        new CatalogAndSchema("Cat1", "Schema1") | "CaT1"  | "SCHeMA1"  | "null.null"    | "null.null"

    }

    def "toString test"() {
        expect:
        schema.toString() == expected

        where:
        schema | expected
        new CatalogAndSchema(null, null)        | "DEFAULT.DEFAULT"
        new CatalogAndSchema("Cat1", null)      | "Cat1.DEFAULT"
        new CatalogAndSchema(null, "Schema1")   | "DEFAULT.Schema1"
        new CatalogAndSchema("Cat1", "Schema1") | "Cat1.Schema1"

    }

}
