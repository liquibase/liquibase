package liquibase.ui

import spock.lang.Specification

class DefaultInputHandlerTest extends Specification {

    def "happy path"() {
        given:
        def handler = new DefaultInputHandler<Integer>()

        when:
        def input = handler.parseInput("10", Integer)

        then:
        input == 10
    }


    def "if exception is thrown then prefix is added to its message"() {
        given:
        def handler = new DefaultInputHandler<Integer>()

        when:
        handler.parseInput("x", Integer)

        then:
        def thrown = thrown(IllegalArgumentException)
        thrown.message == "Invalid value: 'x': For input string: \"x\""
    }

    def "if exception has cause with message then invalid value prefix is added into a cause message"() {
        given:
        def exception = new IllegalArgumentException(new NumberFormatException("For input string: \"x\""))

        when:
        def modifiedException = new DefaultInputHandler().addPrefixToExceptionMessage(exception, "x")

        then:
        modifiedException.message == "Invalid value: 'x': For input string: \"x\""
    }

    def "if exception has cause without message and has its own message then invalid value prefix is added into its own message"() {
        given:
        def exception = new IllegalArgumentException("Dummy error message", new NumberFormatException())

        when:
        def modifiedException = new DefaultInputHandler().addPrefixToExceptionMessage(exception, "x")

        then:
        modifiedException.message == "Invalid value: 'x': Dummy error message"
    }

    def "if exception does not have a cause but has its own message then invalid value prefix is added into its own message"() {
        given:
        def exception = new IllegalArgumentException("Dummy error message")

        when:
        def modifiedException = new DefaultInputHandler().addPrefixToExceptionMessage(exception, "x")

        then:
        modifiedException.message == "Invalid value: 'x': Dummy error message"
    }

    def "if exception has cause without message and its message is not specified it will anyway have a message then prefix is added into its own message"() {
        given:
        def exception = new IllegalArgumentException(new NumberFormatException())

        when:
        def modifiedException = new DefaultInputHandler().addPrefixToExceptionMessage(exception, "x")

        then:
        modifiedException.message == "Invalid value: 'x': java.lang.NumberFormatException"
    }

    def "if exception does not have a cause and does not have its own message then it is returned without modification"() {
        def exception = new IllegalArgumentException()

        when:
        def modifiedException = new DefaultInputHandler().addPrefixToExceptionMessage(exception, "x")

        then:
        modifiedException.message == null
    }
}
