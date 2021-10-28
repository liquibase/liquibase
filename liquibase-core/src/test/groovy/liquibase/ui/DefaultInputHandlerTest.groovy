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
}
