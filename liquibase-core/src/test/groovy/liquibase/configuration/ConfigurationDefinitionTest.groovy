package liquibase.configuration

import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class ConfigurationDefinitionTest extends Specification {

    def "Can build and register"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test.can-build")
                .define("test-property", String)
                .setDefaultValue("Default Value")
                .setDescription("A description here")
                .build()

        then:
        assert Scope.currentScope.getSingleton(LiquibaseConfiguration).getRegisteredDefinitions().contains(definition)
        definition.key == "test.can-build.test-property"
        definition.defaultValue == "Default Value"
        definition.description == "A description here"
    }

    @Unroll
    def "getCurrentValueDetails"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test")
                .define(key, String)
                .addAliasKey("other")
                .setDefaultValue(defaultValue)
                .buildTemporary()

        System.setProperty("test.current-value", "From from system")
        System.setProperty("test.other", "Alias set in system")
        def currentValue = Scope.child(["test.current-value": "From scope"], new Scope.ScopedRunnerWithReturn<CurrentValue>() {
            @Override
            CurrentValue run() throws Exception {
                return definition.getCurrentValueDetails()
            }
        })

        then:
        currentValue.value == expectedValue
        currentValue.source.describe() == expectedSource
        currentValue.getDefaultValueUsed() == defaultValueUsed

        where:
        key             | defaultValue         | expectedValue        | expectedSource                                                   | defaultValueUsed
        "current-value" | "Default Value"      | "From scope"         | "Scoped value 'test.current-value'"                              | false
        "unset-value"   | "Configured Default" | "Configured Default" | "Default value for 'test.unset-value'"                           | true
        "unset-value"   | null                 | null                 | "No configuration or default value found for 'test.unset-value'" | false

    }

    @Unroll
    def "getCurrentValueDetails for aliases"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test")
                .define("actual", String)
                .addAliasKey("other.prefix.key")
                .addAliasKey("other")
                .buildTemporary()

        def currentValue = Scope.child([(key): "From Scope"], new Scope.ScopedRunnerWithReturn<CurrentValue>() {
            @Override
            CurrentValue run() throws Exception {
                return definition.getCurrentValueDetails()
            }
        })

        then:
        currentValue.value == expected

        where:
        key                | expected
        "test.actual"      | "From Scope"
        "other.prefix.key" | "From Scope"
        "other"            | "From Scope"

    }

    def "getValueObfuscated"() {
        when:
        def obfuscated = new ConfigurationDefinition.Builder("test")
                .define("obfuscated", String)
                .setValueObfuscator(new ConfigurationValueObfuscator<String>() {
                    @Override
                    String obfuscate(String value) {
                        return "OBFUSCATED " + value
                    }
                })
                .buildTemporary()

        def plainText = new ConfigurationDefinition.Builder("test")
                .define("obfuscated", String)
                .buildTemporary()

        def obfuscatedOutput = Scope.child(["test.obfuscated": input], new Scope.ScopedRunnerWithReturn<String>() {
            @Override
            String run() throws Exception {
                return obfuscated.getCurrentValueObfuscated()
            }
        })

        def plainOutput = Scope.child(["test.obfuscated": input], new Scope.ScopedRunnerWithReturn<String>() {
            @Override
            String run() throws Exception {
                return plainText.getCurrentValueObfuscated()
            }
        })

        then:
        obfuscatedOutput == expected
        plainOutput == expectedPlain

        where:
        input   | expected           | expectedPlain
        "value" | "OBFUSCATED value" | "value"
        null    | "OBFUSCATED null"  | null

    }
}
