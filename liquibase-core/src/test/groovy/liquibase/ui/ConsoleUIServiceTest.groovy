package liquibase.ui


import liquibase.ui.ConsoleUIService
import spock.lang.Specification
import spock.lang.Unroll

class ConsoleUIServiceTest extends Specification {

    @Unroll
    def "prompt"() {
        when:
        def passedInput = input
        def uiService = new ConsoleUIService(new MockConsoleWrapper(passedInput))
        def outputStream = new ByteArrayOutputStream()
        uiService.setOutputStream(new PrintStream(outputStream))

        def seenOutput = uiService.prompt("Prompt here", defaultValue, null, type)

        then:
        seenOutput == expectedOutput
        outputStream.toString().trim().replaceAll("\r", "") == expectedPrompts.trim().replaceAll("\r", "")

        where:
        input                     | defaultValue     | expectedOutput   | expectedPrompts                                                      | type
        "a string"                | "something else" | "a string"       | "Prompt here (default \"something else\"): "                         | String
        ""                        | "something else" | "something else" | "Prompt here (default \"something else\"): "                         | String
        ""                        | null             | null             | "Prompt here: "                                                      | String
        "x"                       | null             | "x"              | "Prompt here: "                                                      | String
        "1234"                    | null             | 1234             | "Prompt here: "                                                      | Integer
        ["x", "1234"] as String[] | 0                | 1234             | "Prompt here (default \"0\"): \nInvalid value: \"x\"\nPrompt here: " | Integer
        "true"                    | false            | true             | "Prompt here (default \"false\"): "                                  | Boolean
        "false"                   | false            | false            | "Prompt here (default \"false\"): "                                  | Boolean
    }
}
