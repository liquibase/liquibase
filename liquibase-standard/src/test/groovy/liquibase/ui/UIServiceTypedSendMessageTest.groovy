package liquibase.ui

import liquibase.ExtensibleObject
import liquibase.ExtensibleObjectAttribute
import spock.lang.Specification

/**
 * A-5: Verifies that UIService.sendMessage(String, MessageType) default delegates to
 * the plain sendMessage(String) overload, keeping existing UIService impls unaffected.
 *
 * RED phase: fails until A-6 adds MessageType + the default method to UIService.
 */
class UIServiceTypedSendMessageTest extends Specification {

    /**
     * Spy-style class: only overrides sendMessage(String) so we can verify it was called.
     * Does NOT override sendMessage(String, MessageType) — relies on the interface default.
     *
     * Extends ConsoleUIService because UIService requires many method impls and ExtensibleObject
     * plumbing that ConsoleUIService already provides.
     */
    static class SpyUIService extends ConsoleUIService {
        final List<String> received = []

        @Override
        void sendMessage(String message) {
            received << message
        }
    }

    def "sendMessage(String, MessageType.SUCCESS) default delegates to sendMessage(String)"() {
        given:
        def svc = new SpyUIService()

        when:
        svc.sendMessage("command was executed successfully.", UIService.MessageType.SUCCESS)

        then: "the plain sendMessage(String) was invoked with the original string"
        svc.received == ["command was executed successfully."]
    }

    def "sendMessage(String, MessageType) does not alter the message"() {
        given:
        def svc = new SpyUIService()

        when:
        svc.sendMessage("some other message", UIService.MessageType.SUCCESS)

        then:
        svc.received == ["some other message"]
    }
}
