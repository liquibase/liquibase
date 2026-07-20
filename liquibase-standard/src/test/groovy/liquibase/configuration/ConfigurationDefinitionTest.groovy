package liquibase.configuration

import liquibase.Scope
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class ConfigurationDefinitionTest extends Specification {

    def cleanup() {
        System.clearProperty("test.property")
    }

    @Unroll
    def "does not allow invalid keys"() {
        when:
        new ConfigurationDefinition.Builder(prefix).define(property, String).addAliasKey(alias)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == expectedMessage

        where:
        prefix           | property      | alias           | expectedMessage
        "test"           | "invalid-key" | "valid"         | "Invalid key format: test.invalid-key"
        "invalid-prefix" | "invalid-key" | "valid"         | "Invalid prefix format: invalid-prefix"
        "invalid-prefix" | "validValue"  | "valid"         | "Invalid prefix format: invalid-prefix"
        "validPrefix"    | "validValue"  | "invalid-alias" | "Invalid alias format: invalid-alias"
    }

    def "Can build and register"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test.canBuild")
                .define("testProperty", String)
                .setDefaultValue("Default Value")
                .setDescription("A description here")
                .build()

        then:
        assert Scope.currentScope.getSingleton(LiquibaseConfiguration).getRegisteredDefinitions(false).contains(definition)
        definition.key == "test.canBuild.testProperty"
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

        System.setProperty("test.currentValue", "From from system")
        System.setProperty("test.other", "Alias set in system")
        def currentValue = Scope.child(["test.currentValue": "From scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return definition.getCurrentConfiguredValue()
            }
        })

        then:
        currentValue.value == expectedValue
        currentValue.getProvidedValue().describe() == expectedSource
        currentValue.wasDefaultValueUsed() == defaultValueUsed

        where:
        key            | defaultValue         | expectedValue        | expectedSource                                              | defaultValueUsed
        "currentValue" | "Default Value"      | "From scope"         | "Scoped value 'test.currentValue'"                          | false
        "unsetValue"   | "Configured Default" | "Configured Default" | "Default value 'test.unsetValue'"                           | true
        "unsetValue"   | null                 | null                 | "No configured value found 'test.unsetValue'" | false

    }

    @Unroll
    def "getCurrentValueDetails for aliases"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test")
                .define("actual", String)
                .addAliasKey("other.prefix.key")
                .addAliasKey("other")
                .buildTemporary()

        def currentValue = Scope.child([(key): "From Scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return definition.getCurrentConfiguredValue()
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

    def convertValues() {
        when:
        def definition = new ConfigurationDefinition.Builder("test")
                .define("property", Boolean)
                .buildTemporary()

        System.setProperty("test.property", "true")

        then:
        definition.getCurrentConfiguredValue().getValue() == Boolean.TRUE

    }

    def equalsAndHash() {
        when:
        def definition1 = new ConfigurationDefinition.Builder("test")
                .define("property1", Boolean)
                .buildTemporary()

        def definition2 = new ConfigurationDefinition.Builder("test")
                .define("property2", Boolean)
                .buildTemporary()

        def dupeDefinition1 = new ConfigurationDefinition.Builder("test")
                .define("property1", Boolean)
                .buildTemporary()

        then:
        definition1.equals(definition1)
        definition1.hashCode() == definition1.hashCode()

        !definition1.equals(definition2)
        definition1.hashCode() != definition2.hashCode()

        definition1.equals(dupeDefinition1)
        definition1.hashCode() == dupeDefinition1.hashCode()

    }

    @Unroll
    def equalsKey() {
        expect:
        LiquibaseCommandLineConfiguration.SHOULD_RUN.equalsKey(input) == expected

        where:
        input                  | expected
        "liquibase.shouldRun"  | true
        "liquibase.otherValue" | false
        "liquibase.SHOULDRUN"  | true
        "should.run"           | true
        "SHOULD.RUN"           | true
        null                   | false
    }

    @Unroll
    def "referenceScoped resolves from the .reference. sibling only while the reference-connection scope is set"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test.refScope")
                .define("auth.type", String)
                .referenceScoped()
                .buildTemporary()

        def scopeVars = new HashMap<String, Object>()
        if (primary != null) {
            scopeVars.put("test.refScope.auth.type", primary)
        }
        if (reference != null) {
            scopeVars.put("test.refScope.reference.auth.type", reference)
        }
        if (isReference) {
            scopeVars.put(ConfigurationDefinition.IS_REFERENCE_CONNECTION_SCOPE_KEY, Boolean.TRUE)
        }

        def value = Scope.child(scopeVars, { definition.getCurrentValue() } as Scope.ScopedRunnerWithReturn)

        then:
        value == expected

        where:
        isReference | primary | reference | expected
        false       | "PKI"   | "OAUTH"   | "PKI"   // primary connection ignores the reference sibling
        true        | "PKI"   | null      | "PKI"   // unset reference inherits the primary value
        true        | "PKI"   | "OAUTH"   | "OAUTH" // reference sibling overrides the primary
        true        | "PKI"   | "DEFAULT" | null    // sentinel opts out -> resolves as unset (no default)
        true        | "PKI"   | "default" | null    // sentinel is case-insensitive
        true        | null    | "OAUTH"   | "OAUTH" // reference-only configuration
        false       | null    | "OAUTH"   | null    // primary side with no primary value
    }

    @Unroll
    def "referenceScoped DEFAULT sentinel resolves to the definition's default value when one is set"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test.refScopeDefault")
                .define("auth.type", String)
                .setDefaultValue("PWD")
                .referenceScoped()
                .buildTemporary()

        def scopeVars = new HashMap<String, Object>()
        scopeVars.put("test.refScopeDefault.auth.type", "PKI") // primary is set to a non-default value
        if (reference != null) {
            scopeVars.put("test.refScopeDefault.reference.auth.type", reference)
        }
        scopeVars.put(ConfigurationDefinition.IS_REFERENCE_CONNECTION_SCOPE_KEY, Boolean.TRUE)

        def value = Scope.child(scopeVars, { definition.getCurrentValue() } as Scope.ScopedRunnerWithReturn)

        then:
        value == expected

        where:
        reference | expected
        "OAUTH"   | "OAUTH" // explicit reference value wins over the default
        null      | "PKI"   // unset reference inherits the primary (not the default)
        "DEFAULT" | "PWD"   // sentinel opts out -> the definition's default, NOT the primary and NOT null
    }

    def "referenceScoped is inert for definitions that were not flagged"() {
        when:
        def definition = new ConfigurationDefinition.Builder("test.refScopeUnflagged")
                .define("auth.type", String)
                .buildTemporary()

        def value = Scope.child([
                "test.refScopeUnflagged.auth.type"          : "PKI",
                "test.refScopeUnflagged.reference.auth.type" : "OAUTH",
                (ConfigurationDefinition.IS_REFERENCE_CONNECTION_SCOPE_KEY): Boolean.TRUE
        ], { definition.getCurrentValue() } as Scope.ScopedRunnerWithReturn)

        then:
        value == "PKI"
    }
}
