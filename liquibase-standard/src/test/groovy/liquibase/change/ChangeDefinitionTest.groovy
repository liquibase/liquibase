package liquibase.change

import liquibase.Scope
import liquibase.util.StringUtil
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import spock.lang.Unroll

class ChangeDefinitionTest extends Specification {

    Map<String, String> changeDefinitions = new Yaml().load((InputStream) this.class.getClassLoader().getResourceAsStream("liquibase/change/ChangeDefinitionTest.yaml"))

    def setupSpec() {
        //reset change metadata in case other tests modified it and didn't clean up correctly
        Scope.currentScope.getSingleton(ChangeFactory).cachedMetadata.clear()
    }

    /**
     * Compare the current change metadata with the golden master stored in source as ChangeDefinitionTest.yaml.
     * Helpful for seeing/catching changes to the Change definitions and ensuring docs match the code
     */
    @Unroll
    def "check change attributes: #changeName"() {
        when:
        def definition = ""
        def metaData = Scope.currentScope.getSingleton(ChangeFactory).getChangeMetaData(changeName)
        for (def entry : new TreeMap<>(metaData.getParameters()).entrySet()) {

            def paramMetaData = entry.value
            def supported = new TreeSet<>(paramMetaData.supportedDatabases).join(",")
            def required = new TreeSet<>(paramMetaData.requiredForDatabase).join(",")
            definition += "$entry.key $paramMetaData.dataType ${paramMetaData.since == null ? "" : "(since " + paramMetaData.since + ")"}\n"
            if (paramMetaData.description) {
                definition += "  Description: $paramMetaData.description\n"
            }
            definition += "  Supported: $supported\n"
            if (required) {
                definition += "  Required For: $required\n"

            }
        }

        then:
        assert definition.trim() == StringUtil.trimToEmpty(changeDefinitions[changeName]) : "Change name " + changeName + " does not match"

        where:
        changeName << Scope.currentScope.getSingleton(ChangeFactory).definedChanges
    }
}
