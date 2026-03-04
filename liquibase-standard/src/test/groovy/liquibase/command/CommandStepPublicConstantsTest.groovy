package liquibase.command

import liquibase.util.TestUtil
import spock.lang.Specification

import java.lang.reflect.Modifier

class CommandStepPublicConstantsTest extends Specification {
    def "ensure all COMMAND_NAME and *_ARG fields in CommandStep classes are public static final"(){
        given:
        def commandStepClasses = findCommandStepClasses()
        def violations = []

        when:
        commandStepClasses.each {c ->
            def relevantFields = c.declaredFields.findAll{
                field -> field.name == "COMMAND_NAME" || field.name.endsWith("_ARG")
            }

            relevantFields.each {
                relField ->
                    def modifiers = relField.getModifiers()

                    if (!Modifier.isPublic(modifiers)){
                        violations.add("${c.simpleName}.${relField.name} is not public.")
                    }

                    if (!Modifier.isStatic(modifiers)){
                        violations.add("${c.simpleName}.${relField.name} is not static.")
                    }

                    if (!Modifier.isFinal(modifiers)){
                        violations.add("${c.simpleName}.${relField.name} is not final.")
                    }
            }
        }

        then:
        assert violations.isEmpty(), "Found field modifier violations: \n${violations.join('\n')}"
    }

    private Class<?>[] findCommandStepClasses (){
        def classes = TestUtil.getClasses(liquibase.command.CommandStep).findAll{
            c -> c.simpleName.endsWith("CommandStep") && c.package.name.startsWith("liquibase.command.core")
        }
        return classes
    }
}
