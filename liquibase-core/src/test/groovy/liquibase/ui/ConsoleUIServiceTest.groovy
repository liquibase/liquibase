package liquibase.ui


import spock.lang.Specification
import spock.lang.Unroll

class ConsoleUIServiceTest extends Specification {

    @Unroll
    def "prompt"() {
        when:
        def passedInput = input
        def uiService = new ConsoleUIService(new MockConsoleWrapper(passedInput))
        uiService.allowPrompt = true

        def outputStream = new ByteArrayOutputStream()
        uiService.setOutputStream(new PrintStream(outputStream))

        def seenOutput = uiService.prompt("Prompt here", defaultValue, null, type)

        then:
        seenOutput == expectedOutput
        outputStream.toString().trim().replaceAll("\r", "") == expectedPrompts.trim().replaceAll("\r", "")

        where:
        input                     | defaultValue     | expectedOutput   | expectedPrompts                                                                             | type
        "a string"                | "something else" | "a string"       | "Prompt here [something else]: "                                                            | String
        ""                        | "something else" | "something else" | "Prompt here [something else]: "                                                            | String
        ""                        | null             | null             | "Prompt here: "                                                                             | String
        "x"                       | null             | "x"              | "Prompt here: "                                                                             | String
        "1234"                    | null             | 1234             | "Prompt here: "                                                                             | Integer
        ["x", "1234"] as String[] | 0                | 1234             | "Prompt here [0]: \nInvalid value: 'x': For input string: \"x\"\nPrompt here: "             | Integer
        "true"                    | false            | true             | "Prompt here [false]: "                                                                     | Boolean
        "false"                   | false            | false            | "Prompt here [false]: "                                                                     | Boolean
    }
}
