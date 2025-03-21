package liquibase.structure.core

import liquibase.GlobalConfiguration
import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class SchemaTest extends Specification {
    @Unroll("#featureName: #param")
    def "get/set attributes work"() {
        when:
        def schema = new Schema()
        then:
        schema[param] == defaultValue

        when:
        schema[param] = newValue
        then:
        schema[param] == newValue

        where:
        param     | defaultValue | newValue
        "name"    | null         | "schemaName"
        "default" | true         | true
    }

    @Unroll("#featureName: #schema -> #expected")
    def "toCatalogAndSchema"() {
        expect:
        schema.toCatalogAndSchema().toString() == expected

        where:
        schema                                                                      | expected
        new Schema()                                                                | "DEFAULT.DEFAULT"
        new Schema("cat", "schem")                                                  | "cat.schem"
        new Schema(new Catalog("cat"), "myschem")                                   | "cat.myschem"
        new Schema("cat", null)                                                     | "cat.DEFAULT"
        new Schema("cat", "myschem").setDefault(true)                               | "cat.DEFAULT"
        new Schema(new Catalog("cat"), "myschem").setDefault(true)                  | "cat.DEFAULT"
        new Schema(new Catalog("cat").setDefault(true), "myschem").setDefault(true) | "DEFAULT.DEFAULT"
        new Schema(new Catalog("cat").setDefault(true), "myschem")                  | "DEFAULT.myschem"
    }

    @Unroll("#featureName: #schema -> #expected")
    def "toCatalogAndSchemaWithIncludeSchema"(Schema schema, String expected) {
        when:
        schema.setDefault(true)
        Map<String, Object> scopedValues = new HashMap<>()
        scopedValues.put(GlobalConfiguration.INCLUDE_SCHEMA_NAME_FOR_DEFAULT.key, true)

        then:
        Scope.child(scopedValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                schema.toCatalogAndSchema().toString() == expected
            }
        })

        where:
        schema                                                                      | expected
        new Schema()                                                                | "DEFAULT.DEFAULT"
        new Schema("Platform", "workflow")                                          | "Platform.workflow"
    }
}
