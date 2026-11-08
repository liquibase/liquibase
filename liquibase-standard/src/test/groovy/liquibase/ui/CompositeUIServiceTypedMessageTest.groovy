package liquibase.ui

import spock.lang.Specification

/**
 * Verifies that {@link CompositeUIService} forwards the typed
 * {@link UIService#sendMessage(String, UIService.MessageType)} call to every
 * output service, rather than letting the interface default flatten it to the
 * untyped {@link UIService#sendMessage(String)}. Without this, a wrapping
 * CompositeUIService (as installed by the CLI) would strip the MessageType
 * before it reaches a styling UIService implementation.
 */
class CompositeUIServiceTypedMessageTest extends Specification {

    def "typed sendMessage forwards the MessageType to every output service"() {
        given:
        UIService member = Mock()
        def composite = new CompositeUIService(member, [member])

        when:
        composite.sendMessage("done", UIService.MessageType.SUCCESS)

        then:
        1 * member.sendMessage("done", UIService.MessageType.SUCCESS)
        0 * member.sendMessage("done")
    }
}
